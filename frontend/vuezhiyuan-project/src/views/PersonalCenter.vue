<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { getUserInfo, updateUserInfo, getStudentScores, changePassword } from '@/api/user.js'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(true)
const user = ref({})
const editing = ref(false)
const editForm = ref({})
const saving = ref(false)
const saveMsg = ref('')
const saveOk = ref(false)
const scores = ref([])
const activeTab = ref('info')

const quizLinks = [
  { name: '职业兴趣测评', url: 'https://careerquiz.me/zh' },
  { name: '霍兰德职业测评', url: 'https://huolande.pro/' },
  { name: '就业测评中心', url: 'http://jyzx.jiuye.net/?c=tk_ceping&m=maoding' },
  { name: '情商 EQ 评估', url: 'https://www.butterflytest.com/psychological/eq-assessment' },
  { name: '威廉姆斯创造力评估', url: 'http://xinliceyan.org/zh/williams-creativity-assessment' },
]

onMounted(async () => {
  try {
    const [u, sc] = await Promise.all([
      getUserInfo(auth.userId), getStudentScores(auth.userId),
    ])
    user.value = (u?.data || u) || {}
    const avatarKey = 'my_avatar_' + auth.userId
    user.value.avatarUrl = localStorage.getItem(avatarKey) || user.value.avatarUrl
    scores.value = (sc?.data || sc) || []
  } catch {} finally { loading.value = false }
})

const avatarInput = ref(null)
const avatarErr = ref('')

function triggerAvatar() { avatarInput.value?.click() }

async function onAvatarChange(e) {
  const file = e.target.files[0]
  if (!file) return
  avatarErr.value = ''
  // 压缩到 200x200 以内
  const img = new Image()
  img.src = URL.createObjectURL(file)
  img.onload = async () => {
    const canvas = document.createElement('canvas')
    const size = Math.min(img.width, img.height, 200)
    canvas.width = size; canvas.height = size
    const ctx = canvas.getContext('2d')
    const sx = (img.width - size) / 2; const sy = (img.height - size) / 2
    ctx.drawImage(img, sx, sy, size, size, 0, 0, size, size)
    const base64 = canvas.toDataURL('image/jpeg', 0.7)
    URL.revokeObjectURL(img.src)
    user.value.avatarUrl = base64
    const avatarKey = 'my_avatar_' + auth.userId
    localStorage.setItem(avatarKey, base64)
    window.dispatchEvent(new Event('avatar-updated'))
  }
}

function doLogout() {
  auth.logout()
  router.push('/login')
}

function fmtTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  return d.toLocaleDateString('zh-CN') + ' ' + d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function startEdit() {
  editForm.value = {
    nickname: user.value.nickname || '',
    phone: user.value.phone || '',
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  }
  saveMsg.value = ''
  editing.value = true
}

async function saveEdit() {
  saveMsg.value = ''
  const messages = []

  // 更新基本信息
  try {
    const result = await updateUserInfo({ id: user.value.id, nickname: editForm.value.nickname, phone: editForm.value.phone })
    if (result.code !== 200) {
      saveMsg.value = result.msg || '更新失败'
      saveOk.value = false
      return
    }
    // 从服务端重新拉取确认落库
    const refreshed = await getUserInfo(auth.userId)
    user.value = (refreshed?.data || refreshed) || {}
    messages.push('个人信息已更新')
  } catch (e) {
    saveMsg.value = e.response?.data?.msg || '信息更新失败，请稍后重试'
    saveOk.value = false
    return
  }

  // 如果填了密码，同时修改密码
  let pwdFailed = false
  if (editForm.value.oldPassword || editForm.value.newPassword) {
    if (!editForm.value.oldPassword) { saveMsg.value = '请输入旧密码'; saveOk.value = false; return }
    if (editForm.value.newPassword.length < 6) { saveMsg.value = '新密码至少6位'; saveOk.value = false; return }
    if (editForm.value.newPassword !== editForm.value.confirmPassword) { saveMsg.value = '两次新密码不一致'; saveOk.value = false; return }
    try {
      const r = await changePassword({ oldPassword: editForm.value.oldPassword, newPassword: editForm.value.newPassword })
      if (r.code === 200) messages.push('密码已修改')
      else { saveMsg.value = r.msg || '密码修改失败'; saveOk.value = false; pwdFailed = true }
    } catch (e) { saveMsg.value = e.response?.data?.msg || '密码修改失败'; saveOk.value = false; pwdFailed = true }
  }

  if (pwdFailed) return
  saveOk.value = true; saveMsg.value = messages.join('，')
  editing.value = false
  setTimeout(() => { saveMsg.value = '' }, 3000)
}
</script>

<template>
  <div v-if="!loading" class="h-[calc(100vh-4rem)] flex flex-col p-4 lg:p-6 max-w-7xl mx-auto">
    <!-- 标题 -->
    <div class="flex items-center gap-2 mb-4 lg:mb-6">
      <span class="text-2xl">👤</span>
      <h1 class="text-xl font-bold text-gray-800">个人中心</h1>
    </div>

    <div class="flex-1 flex flex-col lg:flex-row gap-4 lg:gap-6 min-h-0">
      <!-- 左栏：用户卡片 + 测评 -->
      <div class="lg:w-72 flex-shrink-0 flex flex-col gap-3">
        <!-- 用户信息卡片 -->
        <div class="glass-card rounded-2xl p-5 text-center">
          <div class="relative inline-block cursor-pointer group" @click="triggerAvatar">
            <img :src="user.avatarUrl || '/favicon.ico'" class="w-20 h-20 rounded-full mx-auto object-cover ring-4 ring-white/80 group-hover:ring-indigo-200 transition-all" @error="e => e.target.src='/favicon.ico'" />
            <div class="absolute inset-0 rounded-full bg-black/0 group-hover:bg-black/20 flex items-center justify-center transition-all">
              <Icon icon="mdi:camera-plus" class="text-white opacity-0 group-hover:opacity-100 text-xl transition-opacity" />
            </div>
          </div>
          <input ref="avatarInput" type="file" accept="image/*" class="hidden" @change="onAvatarChange" />
          <p v-if="avatarErr" class="text-xs text-red-500 mt-1">{{ avatarErr }}</p>
          <h3 class="font-bold text-gray-800 mt-3 text-base">{{ user.nickname || '用户' }}</h3>
          <p class="text-xs text-gray-400 mt-0.5">{{ user.phone || '未绑定手机' }}</p>
          <span :class="user.status === 1 ? 'bg-green-50 text-green-600' : 'bg-red-50 text-red-500'" class="inline-block mt-2 px-3 py-0.5 rounded-full text-[10px] font-medium">{{ user.status === 1 ? '正常' : '禁用' }}</span>

          <button @click="doLogout" class="mt-4 w-full py-2 text-xs text-red-400 border border-red-200 rounded-lg hover:bg-red-50 transition-colors flex items-center justify-center gap-1">
            <Icon icon="mdi:logout" class="text-sm" /> 退出登录
          </button>
        </div>

        <!-- 测评链接 -->
        <div class="glass-card rounded-2xl p-4">
          <p class="text-[11px] font-semibold text-gray-500 mb-2.5 flex items-center gap-1"><span>🧪</span> 心理 & 职业测评</p>
          <div class="space-y-1">
            <a v-for="link in quizLinks" :key="link.name" :href="link.url" target="_blank" class="block text-xs text-gray-500 hover:text-indigo-600 hover:bg-indigo-50/50 rounded-lg px-2 py-1.5 transition-colors">{{ link.name }}</a>
          </div>
        </div>
      </div>

      <!-- 右栏：Tab 内容 -->
      <div class="flex-1 flex flex-col min-h-0">
        <!-- Tabs -->
        <div class="flex gap-1 mb-4">
          <button :class="activeTab === 'info' ? 'bg-white text-gray-800 shadow-sm' : 'text-gray-400 hover:text-gray-600'"
            class="px-4 py-2 rounded-lg text-sm font-medium transition-all flex items-center gap-1.5" @click="activeTab = 'info'"><Icon icon="mdi:account-outline" class="text-base" /> 基本信息</button>
          <button :class="activeTab === 'scores' ? 'bg-white text-gray-800 shadow-sm' : 'text-gray-400 hover:text-gray-600'"
            class="px-4 py-2 rounded-lg text-sm font-medium transition-all flex items-center gap-1.5" @click="activeTab = 'scores'"><Icon icon="mdi:chart-bar" class="text-base" /> 我的成绩</button>
        </div>

        <!-- 基本信息 Tab -->
        <div v-if="activeTab === 'info'" class="flex-1 glass-card rounded-2xl p-5 lg:p-6">
          <!-- 查看模式 -->
          <div v-if="!editing" class="space-y-4">
            <div class="flex items-center py-3 border-b border-gray-100">
              <span class="w-20 text-xs text-gray-400 flex-shrink-0">昵称</span>
              <span class="text-sm text-gray-700 font-medium">{{ user.nickname || '-' }}</span>
            </div>
            <div class="flex items-center py-3 border-b border-gray-100">
              <span class="w-20 text-xs text-gray-400 flex-shrink-0">手机号</span>
              <span class="text-sm text-gray-700 font-medium">{{ user.phone || '-' }}</span>
            </div>
            <div class="flex items-center py-3 border-b border-gray-100">
              <span class="w-20 text-xs text-gray-400 flex-shrink-0">注册时间</span>
              <span class="text-sm text-gray-700">{{ fmtTime(user.createdAt) }}</span>
            </div>
            <button @click="startEdit" class="mt-4 px-5 py-2 bg-gray-800 text-white rounded-lg text-xs font-medium hover:bg-gray-700 transition-colors flex items-center gap-1.5">
              <Icon icon="mdi:pencil" class="text-sm" /> 编辑信息
            </button>
          </div>

          <!-- 编辑模式 -->
          <div v-else class="max-w-md space-y-4">
            <div>
              <label class="text-xs text-gray-500 mb-1 block">昵称</label>
              <input v-model="editForm.nickname" class="w-full p-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-indigo-300 focus:shadow-[0_0_0_3px_rgba(99,102,241,0.1)]" />
            </div>
            <div>
              <label class="text-xs text-gray-500 mb-1 block">手机号</label>
              <input v-model="editForm.phone" class="w-full p-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-indigo-300 focus:shadow-[0_0_0_3px_rgba(99,102,241,0.1)]" />
            </div>

            <div class="border-t border-gray-100 pt-4 mt-4">
              <p class="text-xs text-gray-400 mb-3">修改密码（留空则不修改）</p>
              <div class="space-y-3">
                <input v-model="editForm.oldPassword" type="password" placeholder="旧密码" class="w-full p-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-indigo-300 focus:shadow-[0_0_0_3px_rgba(99,102,241,0.1)]" />
                <input v-model="editForm.newPassword" type="password" placeholder="新密码（至少6位）" class="w-full p-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-indigo-300 focus:shadow-[0_0_0_3px_rgba(99,102,241,0.1)]" />
                <input v-model="editForm.confirmPassword" type="password" placeholder="确认新密码" class="w-full p-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-indigo-300 focus:shadow-[0_0_0_3px_rgba(99,102,241,0.1)]" />
              </div>
            </div>

            <p v-if="saveMsg" :class="saveOk ? 'text-green-500' : 'text-red-500'" class="text-xs">{{ saveMsg }}</p>
            <div class="flex gap-2">
              <button :disabled="saving" @click="saveEdit" class="px-5 py-2 bg-gray-800 text-white rounded-lg text-xs font-medium hover:bg-gray-700 disabled:opacity-50 transition-colors">{{ saving ? '保存中...' : '保存' }}</button>
              <button @click="editing = false; saveMsg = ''" class="px-4 py-2 text-xs text-gray-500 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors">取消</button>
            </div>
          </div>
        </div>

        <!-- 我的成绩 Tab -->
        <div v-if="activeTab === 'scores'" class="flex-1 glass-card rounded-2xl p-5 lg:p-6 overflow-hidden flex flex-col min-h-0">
          <div v-if="scores.length" class="flex-1 overflow-auto">
            <table class="w-full text-xs">
              <thead>
                <tr class="text-gray-400 text-left border-b border-gray-100">
                  <th class="pb-3 font-medium">年份</th>
                  <th class="pb-3 font-medium">省份</th>
                  <th class="pb-3 font-medium">科类</th>
                  <th class="pb-3 font-medium text-center">总分</th>
                  <th class="pb-3 font-medium text-center">省排名</th>
                  <th class="pb-3 font-medium">选科</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="s in scores" :key="s.id" class="border-b border-gray-50 hover:bg-gray-50/50 transition-colors">
                  <td class="py-3 text-gray-500">{{ s.examYear }}</td>
                  <td class="py-3 text-gray-500">{{ s.provinceCode }}</td>
                  <td class="py-3">
                    <span :class="s.subjectType === 1 ? 'text-blue-600 bg-blue-50' : 'text-amber-600 bg-amber-50'" class="px-1.5 py-0.5 rounded text-[10px] font-medium">{{ s.subjectType === 1 ? '物理类' : s.subjectType === 2 ? '历史类' : '-' }}</span>
                  </td>
                  <td class="py-3 text-center font-bold text-gray-800">{{ s.totalScore }}</td>
                  <td class="py-3 text-center text-gray-500">{{ s.provinceRank?.toLocaleString() || '-' }}</td>
                  <td class="py-3 text-gray-500">{{ s.selectedSubjects?.replace(/,/g, '、') || '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="flex-1 flex items-center justify-center text-gray-400 text-sm">
            <div class="text-center">
              <Icon icon="mdi:file-document-outline" class="text-4xl mx-auto mb-2 opacity-30" />
              <p>暂无成绩记录</p>
              <p class="text-xs text-gray-300 mt-1">请在成绩信息页面录入</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="h-[calc(100vh-4rem)] flex items-center justify-center text-gray-400">加载中...</div>
</template>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px -1px rgba(0, 0, 0, 0.02);
}
</style>
