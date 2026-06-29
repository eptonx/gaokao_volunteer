<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useAuthStore } from '@/stores/auth'
import {
  listPlans, getPlan,
  deletePlan, exportReport,
  listPlanDetails, getRiskAnalysis,
} from '@/api/plan'

const auth = useAuthStore()
const router = useRouter()
const plans = ref([])
const currentPlan = ref(null)
const details = ref([])
const riskAnalysis = ref('')
const showExport = ref(false)
const exportHtml = ref('')
const loading = ref(false)

const stats = computed(() => {
  const schools = new Map()
  for (const d of details.value) {
    const key = (d.institutionId && d.institutionId > 0) ? d.institutionId : d.institutionName
    if (!schools.has(key)) schools.set(key, d.riskLevel)
  }
  return {
    chong: [...schools.values()].filter(r => r === 1).length,
    wen: [...schools.values()].filter(r => r === 2).length,
    bao: [...schools.values()].filter(r => r === 3).length,
  }
})

// 按院校名称分组，同一院校的多个专业归到一起
const groupedDetails = computed(() => {
  const map = new Map()
  for (const d of details.value) {
    const key = (d.institutionId && d.institutionId > 0) ? d.institutionId : d.institutionName
    if (!map.has(key)) {
      map.set(key, {
        institutionId: d.institutionId,
        institutionName: d.institutionName || '待匹配院校',
        institutionLevel: d.institutionLevel,
        riskLevel: d.riskLevel,
        majors: [],
      })
    }
    map.get(key).majors.push({
      majorName: d.majorName || '待匹配专业',
      probability: d.probability || 0,
    })
  }
  return [...map.values()]
})

const riskConf = { 1: { label: '冲刺', color: 'text-red-500', bg: 'bg-red-50', bar: 'bg-red-400', dot: 'bg-red-500' }, 2: { label: '稳妥', color: 'text-orange-500', bg: 'bg-orange-50', bar: 'bg-orange-400', dot: 'bg-orange-500' }, 3: { label: '保底', color: 'text-green-500', bg: 'bg-green-50', bar: 'bg-green-400', dot: 'bg-green-500' } }

function goToInstitution(school) {
  if (school.institutionId && school.institutionId > 0) {
    router.push({ path: '/institution/' + school.institutionId, query: { from: 'plan' } })
  }
}

function fmtDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN')
}

async function loadPlans() {
  try {
    const r = await listPlans(auth.userId)
    plans.value = r.data || []
  } catch (e) {
    console.error('加载方案列表失败:', e)
  }
}

async function loadDetails(planId) {
  loading.value = true
  try {
    const res = await listPlanDetails(planId)
    const list = res.data || res || []
    details.value = list
    if (!list.length) console.warn('方案详情为空, planId:', planId, '原始响应:', res)
  } catch (e) {
    console.error('加载方案详情失败:', e)
    details.value = []
  } finally {
    loading.value = false
  }
}

async function loadRiskAnalysis(planId) {
  try {
    const res = await getRiskAnalysis(planId)
    riskAnalysis.value = res.data || res || ''
  } catch {
    riskAnalysis.value = ''
  }
}

async function selectPlan(p) {
  currentPlan.value = p
  riskAnalysis.value = ''
  await Promise.all([loadDetails(p.id), loadRiskAnalysis(p.id)])
}

async function refreshPlan() {
  if (!currentPlan.value) return
  try {
    const res = await getPlan(currentPlan.value.id)
    currentPlan.value = res.data || res
    const idx = plans.value.findIndex(p => p.id === currentPlan.value.id)
    if (idx >= 0) plans.value[idx] = currentPlan.value
  } catch { /* ignore */ }
  await Promise.all([
    loadDetails(currentPlan.value.id),
    loadRiskAnalysis(currentPlan.value.id),
  ])
}

async function removePlan(id) {
  if (!confirm('确定删除此方案？')) return
  try {
    await deletePlan(id)
  } catch { /* ignore */ }
  plans.value = plans.value.filter((p) => p.id !== id)
  if (currentPlan.value?.id === id) {
    currentPlan.value = null
    details.value = []
  }
}

async function doExport() {
  if (!currentPlan.value) return
  try {
    const res = await exportReport(currentPlan.value.id)
    exportHtml.value = res.data?.html || res.data || ''
    showExport.value = true
  } catch {
    alert('导出失败')
  }
}

function printReport() {
  const w = window.open('', '_blank')
  w.document.body.innerHTML = exportHtml.value
  setTimeout(() => w.print(), 300)
}

onMounted(async () => {
  await loadPlans()
  // 自动选中第一个方案
  if (plans.value.length) {
    await selectPlan(plans.value[0])
  }
})
</script>

<template>
  <div class="flex h-[calc(100vh-4rem)] max-w-7xl mx-auto">
    <!-- Left sidebar: plans -->
    <aside class="w-60 bg-white border-r border-gray-100 flex flex-col flex-shrink-0">
      <div class="flex-1 overflow-y-auto px-3 pt-3">
        <div v-for="p in plans" :key="p.id" @click="selectPlan(p)"
          :class="p.id === currentPlan?.id ? 'border-blue-400 bg-blue-50/50' : 'border-gray-100 hover:border-gray-200'"
          class="p-3 border rounded-xl mb-2 cursor-pointer transition-colors relative group">
          <div class="text-sm font-semibold text-gray-700">{{ p.planName }}</div>
          <div class="flex gap-2 mt-1 text-xs">
            <span :class="p.isLocked ? 'text-orange-500' : 'text-gray-400'">{{ p.isLocked ? '🔒 已锁定' : '📝 草稿' }}</span>
            <span class="text-gray-300">{{ fmtDate(p.createdAt) }}</span>
          </div>
          <button @click.stop="removePlan(p.id)" class="absolute top-2.5 right-2.5 text-gray-300 hover:text-red-400 opacity-0 group-hover:opacity-100 transition-all">×</button>
        </div>
        <p v-if="!plans.length" class="text-xs text-gray-300 text-center mt-8">暂无方案，输入名称后点击生成</p>
      </div>
    </aside>

    <!-- Right: detail -->
    <main class="flex-1 overflow-y-auto min-w-0" v-if="currentPlan">
      <!-- Dark gradient header -->
      <div class="bg-gradient-to-br from-gray-800 to-gray-900 rounded-2xl p-8 m-4 text-white">
        <div class="flex items-start justify-between">
          <div>
            <h2 class="text-2xl font-bold">{{ currentPlan.planName }}</h2>
            <div class="flex items-center gap-3 mt-2 text-sm text-white/70">
              <span :class="currentPlan.isLocked ? 'text-green-400' : 'text-white/50'">{{ currentPlan.isLocked ? '🔒 已锁定' : '📝 草稿' }}</span>
            </div>
          </div>
          <div class="flex gap-2">
            <button @click="refreshPlan" class="px-3 py-2 border border-white/30 text-white/70 rounded-lg text-xs hover:bg-white/10 transition-colors">🔄 刷新</button>
            <button @click="doExport" class="px-3 py-2 border border-white/30 text-white/70 rounded-lg text-xs hover:bg-white/10 transition-colors">📄 导出报告</button>
            <button @click="removePlan(currentPlan.id)" class="px-3 py-2 border border-red-400/50 text-red-300 rounded-lg text-xs hover:bg-red-500/20 transition-colors">🗑 删除</button>
          </div>
        </div>

        <!-- Stats -->
        <div class="grid grid-cols-3 gap-4 mt-6">
          <div class="bg-white/10 rounded-xl p-4 text-center">
            <div class="text-3xl font-bold text-red-300">{{ stats.chong }}</div>
            <div class="text-xs text-white/60 mt-1">冲刺</div>
            <div class="text-xs text-white/30 mt-0.5">30%-60%</div>
          </div>
          <div class="bg-white/10 rounded-xl p-4 text-center">
            <div class="text-3xl font-bold text-orange-300">{{ stats.wen }}</div>
            <div class="text-xs text-white/60 mt-1">稳妥</div>
            <div class="text-xs text-white/30 mt-0.5">60%-85%</div>
          </div>
          <div class="bg-white/10 rounded-xl p-4 text-center">
            <div class="text-3xl font-bold text-green-300">{{ stats.bao }}</div>
            <div class="text-xs text-white/60 mt-1">保底</div>
            <div class="text-xs text-white/30 mt-0.5">85%-99%</div>
          </div>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="text-center py-10 text-gray-400 text-sm">加载中...</div>

      <!-- Detail cards -->
      <template v-else-if="details.length">
        <!-- Risk analysis card -->
        <div v-if="riskAnalysis" class="mx-4 mb-4 bg-gradient-to-r from-yellow-50 to-orange-50 border border-yellow-200 border-l-4 border-l-orange-400 rounded-xl p-4">
          <h3 class="text-sm font-bold text-orange-600 mb-2">⚠️ AI 风险分析</h3>
          <p class="text-sm text-gray-600 leading-relaxed whitespace-pre-wrap">{{ riskAnalysis }}</p>
        </div>

        <div class="px-4 pb-8 space-y-3">
          <div v-for="(school, si) in groupedDetails" :key="si" class="p-4 bg-white rounded-xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow">
            <!-- 院校头部 -->
            <div class="flex items-center gap-3 mb-3">
              <span :class="[riskConf[school.riskLevel]?.bg, riskConf[school.riskLevel]?.color]" class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0">{{ si + 1 }}</span>
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2">
                  <span class="font-semibold text-gray-800" :class="{ 'cursor-pointer hover:underline': school.institutionId > 0 }" @click="goToInstitution(school)">{{ school.institutionName }}</span>
                  <span v-if="school.institutionLevel" class="text-xs px-1.5 py-0.5 bg-blue-50 text-blue-500 rounded">{{ school.institutionLevel }}</span>
                  <span :class="[riskConf[school.riskLevel]?.color, riskConf[school.riskLevel]?.bg]" class="text-xs px-2 py-0.5 rounded-full bg-opacity-10">{{ riskConf[school.riskLevel]?.label }}</span>
                </div>
              </div>
            </div>
            <!-- 专业列表 -->
            <div class="ml-[52px] space-y-1.5">
              <div v-for="(m, mi) in school.majors" :key="mi" class="flex items-center gap-3 py-1.5 px-3 bg-gray-50 rounded-lg">
                <span class="text-xs text-gray-500 w-5 text-right">{{ mi + 1 }}</span>
                <span class="text-sm text-gray-700 flex-1">{{ m.majorName }}</span>
                <div class="w-28 flex items-center gap-2">
                  <div class="flex-1 h-1.5 bg-gray-200 rounded-full overflow-hidden">
                    <div :class="riskConf[school.riskLevel]?.bar" class="h-full rounded-full" :style="{ width: m.probability + '%' }"></div>
                  </div>
                  <span class="text-xs text-gray-400 w-10 text-right">{{ m.probability }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- Empty detail: no data generated -->
      <div v-else class="px-4 pb-8 text-center py-16">
        <Icon icon="mdi:file-document-outline" class="text-5xl text-gray-200 mx-auto mb-4" />
        <p class="text-gray-400 text-sm mb-2">方案详情为空</p>
        <p class="text-xs text-gray-300 mb-4">可能原因：数据库暂缺该省份的录取数据，或成绩未录入</p>
        <button @click="refreshPlan" class="px-5 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition-colors flex items-center gap-1.5 mx-auto">
          <Icon icon="mdi:refresh" /> 重新加载
        </button>
      </div>
    </main>

    <!-- Empty state: no plan selected -->
    <main class="flex-1 flex items-center justify-center text-gray-400" v-else>
      <div class="text-center">
        <Icon icon="mdi:clipboard-text-outline" class="text-5xl mx-auto mb-3" />
        <p class="text-sm">选择一个方案查看详情</p>
        <p class="text-xs text-gray-300 mt-1">从左侧选择一个方案查看详情</p>
      </div>
    </main>

    <!-- Export modal -->
    <div v-if="showExport" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center" @click.self="showExport = false">
      <div class="bg-white rounded-2xl p-6 w-[700px] max-h-[80vh] overflow-y-auto">
        <h3 class="font-semibold text-lg mb-4">报告预览</h3>
        <div class="border border-gray-200 rounded-xl p-4 max-h-[50vh] overflow-y-auto" v-html="exportHtml"></div>
        <div class="flex gap-3 mt-4">
          <button @click="printReport" class="px-5 py-2.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition-colors">🖨 打印 / 导出 PDF</button>
          <button @click="showExport = false" class="px-5 py-2.5 bg-gray-100 text-gray-600 rounded-lg text-sm hover:bg-gray-200 transition-colors">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>
