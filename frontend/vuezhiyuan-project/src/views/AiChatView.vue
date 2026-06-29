<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRouter, onBeforeRouteLeave } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useAuthStore } from '@/stores/auth'
import { listSessions, getHistory, deleteSession, chatStream } from '@/api/ai'
import { generatePlan } from '@/api/plan'

const router = useRouter()
const auth = useAuthStore()
const msgBox = ref(null)
const input = ref('')
const messages = ref([])
const currentSessionId = ref('')
const sessions = ref([])         // [{ sessionId, title, lastMsgTime }]
const streaming = ref(false)
const streamText = ref('')

// ========== 生成方案弹窗 ==========
const showGenModal = ref(false)
const genPlanName = ref('')
const generatingPlan = ref(false)
const genError = ref('')
const genSuccess = ref(false)
const createdPlanId = ref(null)

const quickQuestions = [
  '根据我的成绩能上什么大学？',
  '什么专业就业前景好？',
  '冲稳保怎么分配比较合理？',
  '985和211有什么区别？',
]

function fmtTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString('zh-CN')
}

function newSession() {
  currentSessionId.value = crypto.randomUUID()
  messages.value = []
  sessions.value.unshift({ sessionId: currentSessionId.value, title: '新对话', lastMsgTime: new Date().toISOString() })
}

async function switchSession(sid) {
  currentSessionId.value = sid
  await loadMessages(sid)
}

async function loadMessages(sid) {
  try {
    const res = await getHistory(sid)
    messages.value = res.data || []
  } catch {
    messages.value = []
  }
  await nextTick()
  scrollBottom()
}

async function loadSessions() {
  try {
    const res = await listSessions()
    const list = res.data || []
    sessions.value = list.map(item => ({
      sessionId: typeof item === 'string' ? item : (item.session_id || item.sessionId || item),
      title: item.title || null,
      lastMsgTime: item.last_msg_time || item.lastMsgTime || null,
    }))
    if (sessions.value.length && !currentSessionId.value) {
      currentSessionId.value = sessions.value[0].sessionId
      await loadMessages(currentSessionId.value)
    }
  } catch {
    sessions.value = []
  }
}

async function removeSession(sid) {
  try {
    await deleteSession(sid)
  } catch { /* ignore */ }
  sessions.value = sessions.value.filter((s) => s.sessionId !== sid)
  messages.value = []
  if (currentSessionId.value === sid) {
    currentSessionId.value = sessions.value[0]?.sessionId || ''
    if (currentSessionId.value) await loadMessages(currentSessionId.value)
  }
}

async function send() {
  const text = input.value.trim()
  if (!text || streaming.value) return
  input.value = ''
  sendQuick(text)
}

async function sendQuick(text) {
  if (streaming.value) return

  if (!currentSessionId.value) newSession()

  const sess = sessions.value.find(s => s.sessionId === currentSessionId.value)
  if (sess && !sess.title) {
    sess.title = text.length > 20 ? text.slice(0, 20) + '...' : text
    sess.lastMsgTime = new Date().toISOString()
  }

  messages.value.push({ role: 1, content: text })
  streaming.value = true; streamText.value = ''

  try {
    const resp = await chatStream(
      { message: text, sessionId: currentSessionId.value },
      auth.accessToken,
    )

    if (!resp.ok) {
      throw new Error('HTTP ' + resp.status)
    }

    const reader = resp.body.getReader()
    const decoder = new TextDecoder()
    let aiContent = ''
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      const blocks = buffer.split('\n\n')
      buffer = blocks.pop() || ''

      for (const block of blocks) {
        if (!block.trim()) continue
        const lines = block.split('\n')
        let eventType = 'message'
        let data = ''

        for (const line of lines) {
          if (line.startsWith('event:')) {
            eventType = line.slice(6).trim()
          } else if (line.startsWith('data:')) {
            data = line.slice(5).trim()
          }
        }

        if (eventType === 'error' || data === '[DONE]') {
          // 错误或结束
        } else if (eventType === 'session') {
          // session 事件，忽略
        } else if (data) {
          streamText.value += data
          aiContent += data
        }
      }
      await nextTick(); scrollBottom()
    }

    if (aiContent) messages.value.push({ role: 2, content: aiContent })
  } catch (err) {
    console.error('SSE error:', err)
    messages.value.push({ role: 2, content: '[回复失败，请重试]' })
  } finally { streaming.value = false; streamText.value = ''; await nextTick(); scrollBottom() }
}

// ========== 生成方案 ==========
function openGenModal() {
  // 自动生成方案名称：取当前会话标题或第一条用户消息
  const userMsg = messages.value.find(m => m.role === 1)
  const titleHint = sessions.value.find(s => s.sessionId === currentSessionId.value)?.title
  genPlanName.value = titleHint && titleHint !== '新对话'
    ? titleHint + ' · 志愿方案'
    : (userMsg ? userMsg.content.slice(0, 15) + ' · 志愿方案' : 'AI 志愿方案')
  genError.value = ''
  genSuccess.value = false
  createdPlanId.value = null
  showGenModal.value = true
}

async function doGeneratePlan() {
  if (!genPlanName.value.trim()) return
  generatingPlan.value = true
  genError.value = ''
  try {
    const res = await generatePlan(genPlanName.value.trim(), currentSessionId.value)
    // 检查业务状态码（后端返回 Result.error 时 HTTP 仍是 200）
    if (res.code && res.code !== 200) {
      genError.value = res.msg || res.message || '生成失败'
      return
    }
    const plan = res.data
    if (!plan || !plan.id) {
      genError.value = '方案生成失败：未返回有效数据'
      return
    }
    createdPlanId.value = plan.id
    genSuccess.value = true
  } catch (e) {
    genError.value = e.response?.data?.message || e.response?.data?.msg || e.message || '生成失败，请确认已录入成绩'
  } finally {
    generatingPlan.value = false
  }
}

function goToPlan() {
  showGenModal.value = false
  if (createdPlanId.value) {
    router.push('/plan')
  }
}

function scrollBottom() { if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight }
// AI 生成中禁止跳转，避免流式响应丢失
onBeforeRouteLeave((_to, _from, next) => {
  if (streaming.value) {
    alert('AI 正在生成回复中，请等待完成后再切换页面')
    next(false)
  } else {
    next()
  }
})

onMounted(loadSessions)
</script>

<template>
  <div class="flex h-[calc(100vh-4rem)] max-w-7xl mx-auto">
    <!-- Left sidebar: sessions -->
    <aside class="w-52 bg-white border-r border-gray-100 flex flex-col flex-shrink-0">
      <button @click="newSession" class="m-3 py-2.5 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors flex items-center justify-center gap-1">
        <Icon icon="mdi:plus" /> 新对话
      </button>
      <div class="flex-1 overflow-y-auto px-2">
        <div v-for="s in sessions" :key="s.sessionId" @click="switchSession(s.sessionId)"
          :class="s.sessionId === currentSessionId ? 'bg-blue-50 text-blue-700' : 'text-gray-600 hover:bg-gray-50'"
          class="flex items-center justify-between px-3 py-2.5 rounded-lg cursor-pointer text-xs mb-1 transition-colors group">
          <div class="min-w-0">
            <div class="truncate font-medium">{{ s.title || '新对话' }}</div>
            <div class="text-gray-400 text-[10px]">{{ fmtTime(s.lastMsgTime) }}</div>
          </div>
          <button @click.stop="removeSession(s.sessionId)" class="text-gray-300 hover:text-red-400 opacity-0 group-hover:opacity-100 transition-all ml-1">×</button>
        </div>
        <p v-if="!sessions.length" class="text-xs text-gray-300 text-center mt-8">暂无对话</p>
      </div>
    </aside>

    <!-- Right: chat area -->
    <main class="flex-1 flex flex-col min-w-0">
      <!-- Chat header with generate plan button -->
      <div v-if="messages.length" class="flex items-center justify-end px-5 py-2 bg-white border-b border-gray-100">
        <button @click="openGenModal" :disabled="streaming"
          class="px-4 py-1.5 bg-gradient-to-r from-orange-400 to-red-500 text-white rounded-full text-xs font-medium hover:from-orange-500 hover:to-red-600 disabled:opacity-50 transition-all flex items-center gap-1.5 shadow-sm">
          <Icon icon="mdi:clipboard-text-outline" class="text-sm" /> 生成志愿方案
        </button>
      </div>

      <!-- Messages -->
      <div ref="msgBox" class="flex-1 overflow-y-auto p-5 space-y-4">
        <div v-for="(m, i) in messages" :key="i" :class="m.role === 1 ? 'justify-end' : 'justify-start'" class="flex">
          <div :class="[m.role === 1 ? 'bg-blue-600 text-white rounded-br-md' : 'bg-white border border-gray-200 text-gray-700 rounded-bl-md', 'max-w-[70%] px-4 py-2.5 rounded-2xl text-sm leading-relaxed whitespace-pre-wrap']">{{ m.content }}</div>
        </div>
        <!-- Streaming bubble -->
        <div v-if="streaming" class="flex justify-start">
          <div class="max-w-[70%] px-4 py-2.5 bg-white border border-gray-200 text-gray-700 rounded-2xl rounded-bl-md text-sm">
            {{ streamText }}<span class="animate-pulse text-blue-500">|</span>
          </div>
        </div>
        <!-- Empty state -->
        <div v-if="!messages.length && !streaming" class="flex flex-col items-center justify-center h-full text-gray-300 gap-3">
          <Icon icon="mdi:robot-outline" class="text-6xl" />
          <p class="text-sm">输入消息开始 AI 对话</p>
          <div class="flex flex-wrap gap-2 justify-center max-w-md mt-2">
            <button v-for="q in quickQuestions" :key="q" @click="sendQuick(q)"
              class="px-3 py-1.5 bg-white border border-gray-200 rounded-full text-xs text-gray-400 hover:text-blue-500 hover:border-blue-300 transition-colors">{{ q }}</button>
          </div>
        </div>
      </div>

      <!-- Input area -->
      <div class="p-4 bg-white border-t border-gray-100">
        <div class="flex gap-3">
          <textarea v-model="input" :disabled="streaming" @keydown.enter.exact.prevent="send"
            placeholder="输入消息，Enter 发送..." rows="2"
            class="flex-1 p-3 border border-gray-200 rounded-xl text-sm outline-none focus:border-blue-500 resize-none disabled:bg-gray-50"></textarea>
          <button @click="send" :disabled="streaming || !input.trim()"
            class="px-5 py-2.5 bg-blue-600 text-white rounded-xl text-sm font-medium hover:bg-blue-700 disabled:opacity-50 transition-colors flex-shrink-0">
            <Icon icon="mdi:send" />
          </button>
        </div>
      </div>
    </main>

    <!-- 生成方案弹窗 -->
    <Teleport to="body">
      <div v-if="showGenModal" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center" @click.self="showGenModal = false">
        <div class="bg-white rounded-2xl p-6 w-[420px] shadow-xl">
          <h3 class="text-lg font-bold text-gray-800 mb-1">
            {{ genSuccess ? '🎉 方案已生成' : '📋 生成志愿方案' }}
          </h3>
          <p class="text-xs text-gray-400 mb-4">
            {{ genSuccess ? 'AI 已根据你的成绩自动生成冲稳保梯度方案' : 'AI 将根据你的成绩和历年录取数据，自动生成冲稳保梯度方案' }}
          </p>

          <!-- 未生成：输入方案名 -->
          <div v-if="!genSuccess">
            <label class="text-xs text-gray-500 mb-1.5 block">方案名称</label>
            <input v-model="genPlanName" :disabled="generatingPlan"
              class="w-full p-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-blue-500 mb-3"
              placeholder="输入方案名称..." />
            <p v-if="genError" class="text-red-500 text-xs mb-3">{{ genError }}</p>
            <div class="flex gap-3">
              <button @click="doGeneratePlan" :disabled="generatingPlan || !genPlanName.trim()"
                class="flex-1 py-2.5 bg-gradient-to-r from-orange-400 to-red-500 text-white rounded-lg text-sm font-medium hover:from-orange-500 hover:to-red-600 disabled:opacity-50 transition-all flex items-center justify-center gap-1.5">
                <Icon v-if="generatingPlan" icon="mdi:loading" class="animate-spin" />
                {{ generatingPlan ? 'AI 生成中...' : '🤖 开始生成' }}
              </button>
              <button @click="showGenModal = false" :disabled="generatingPlan"
                class="px-5 py-2.5 bg-gray-100 text-gray-500 rounded-lg text-sm hover:bg-gray-200 transition-colors">取消</button>
            </div>
          </div>

          <!-- 生成成功 -->
          <div v-else>
            <div class="bg-green-50 border border-green-200 rounded-xl p-4 mb-4 text-sm text-green-700">
              ✅ 方案 <strong>{{ genPlanName }}</strong> 已生成并自动存档。<br>
              <span class="text-xs text-green-500 mt-1 block">包含冲刺/稳妥/保底三级梯度 + AI 风险分析</span>
            </div>
            <div class="flex gap-3">
              <button @click="goToPlan"
                class="flex-1 py-2.5 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors flex items-center justify-center gap-1.5">
                <Icon icon="mdi:clipboard-text-outline" /> 查看方案详情
              </button>
              <button @click="showGenModal = false"
                class="px-5 py-2.5 bg-gray-100 text-gray-500 rounded-lg text-sm hover:bg-gray-200 transition-colors">继续对话</button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
