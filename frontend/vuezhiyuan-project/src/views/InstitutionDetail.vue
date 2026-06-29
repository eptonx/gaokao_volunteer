<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import { TooltipComponent, GridComponent, LegendComponent, TitleComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getInstitutionDetail, getInstitutionMajors, getInstitutionScores, getInstitutionGuides } from '@/api/institution.js'
import { addFavorite, cancelFavorite, checkFavorite } from '@/api/favorite.js'
import { useAuthStore } from '@/stores/auth'

use([BarChart, LineChart, TooltipComponent, GridComponent, LegendComponent, TitleComponent, CanvasRenderer])

const route = useRoute(); const router = useRouter(); const auth = useAuthStore()
const id = Number(route.params.id)

const institution = ref(null); const majors = ref([]); const scores = ref([]); const guides = ref([])
const loading = ref(true); const isFavorited = ref(false); const activeTab = ref('majors'); const guideTab = ref(0)

const natureMap = { 1: '公办', 2: '民办', 3: '独立学院', 4: '中外合作' }

onMounted(async () => {
  try {
    const [i, m, s, g, f] = await Promise.all([
      getInstitutionDetail(id), getInstitutionMajors(id), getInstitutionScores(id), getInstitutionGuides(id), checkFavorite(auth.userId, id, 1),
    ])
    institution.value = i?.data || i; majors.value = (m?.data || m) || []; scores.value = (s?.data || s) || []; guides.value = (g?.data || g) || []
    isFavorited.value = !!(f?.data ?? f)
  } catch {} finally { loading.value = false }
})

const shareCopied = ref(false)
async function shareInstitution() {
  try {
    await navigator.clipboard.writeText(window.location.href)
    shareCopied.value = true
    setTimeout(() => shareCopied.value = false, 2000)
  } catch {}
}

async function toggleFav() {
  try {
    if (isFavorited.value) { await cancelFavorite(auth.userId, id, 1); isFavorited.value = false }
    else { await addFavorite(auth.userId, id, 1); isFavorited.value = true }
  } catch {}
}

const chartMajor = ref('all')
const chartMajors = computed(() => [...new Set(scores.value.map(s => s.majorName).filter(Boolean))])

// Chart data: group scores by year
const chartOption = computed(() => {
  const filtered = chartMajor.value === 'all' ? scores.value : scores.value.filter(s => s.majorName === chartMajor.value)
  const byYear = {}
  filtered.forEach(s => {
    const y = s.year || s.admissionYear
    if (!byYear[y]) byYear[y] = { min: 9999, max: 0, sum: 0, count: 0 }
    const r = byYear[y]
    if (s.minScore && s.minScore < r.min) r.min = s.minScore
    if (s.maxScore && s.maxScore > r.max) r.max = s.maxScore
    if (s.avgScore) { r.sum += s.avgScore; r.count++ }
  })
  const years = Object.keys(byYear).sort()
  const titleText = chartMajor.value === 'all' ? '全部专业录取分趋势' : `${chartMajor.value} 录取分趋势`
  return {
    title: { text: titleText, left: 'center', textStyle: { fontSize: 13, color: '#374151' } },
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, textStyle: { fontSize: 11 } },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: years, axisLabel: { fontSize: 11, color: '#999' } },
    yAxis: { type: 'value', name: '分数', axisLabel: { fontSize: 11, color: '#999' } },
    series: [
      { name: '最低分', type: 'line', data: years.map(y => byYear[y].min), itemStyle: { color: '#ef4444' }, symbol: 'circle', symbolSize: 6, lineStyle: { width: 2 } },
      { name: '最高分', type: 'line', data: years.map(y => byYear[y].max), itemStyle: { color: '#3b82f6' }, symbol: 'circle', symbolSize: 6, lineStyle: { width: 2 } },
      ...(chartMajor.value !== 'all' ? [{ name: '平均分', type: 'line', data: years.map(y => byYear[y].count ? (byYear[y].sum / byYear[y].count).toFixed(0) : null), itemStyle: { color: '#10b981' }, symbol: 'diamond', symbolSize: 6, lineStyle: { width: 2, type: 'dashed' } }] : []),
    ],
  }
})
</script>

<template>
  <div v-if="!loading && institution" class="h-[calc(100vh-4rem)] overflow-y-auto max-w-7xl mx-auto p-4 lg:p-6">
    <!-- Back -->
    <a v-if="route.query.from === 'plan'" @click="router.push('/plan')" class="inline-flex items-center gap-1 text-sm text-gray-500 hover:text-blue-600 cursor-pointer mb-4 transition-colors">
      <Icon icon="mdi:arrow-left" /> 返回志愿方案
    </a>
    <a v-else @click="router.push('/search')" class="inline-flex items-center gap-1 text-sm text-gray-500 hover:text-blue-600 cursor-pointer mb-4 transition-colors">
      <Icon icon="mdi:arrow-left" /> 返回院校检索
    </a>

    <div class="flex gap-6">
      <!-- Left: main content 70% -->
      <div class="flex-1 space-y-5 min-w-0">
        <!-- Header card -->
        <div class="flex gap-5 p-6 bg-white rounded-2xl border border-gray-100 shadow-sm items-center">
          <img :src="institution.logoUrl || '/favicon.ico'" class="w-20 h-20 rounded-2xl object-cover bg-gray-100" @error="e => e.target.style.display='none'" />
          <div class="flex-1">
            <h2 class="text-xl font-bold text-gray-800">{{ institution.institutionName }}</h2>
            <div class="flex flex-wrap gap-1.5 mt-2">
              <span v-if="institution.level" class="px-2.5 py-0.5 bg-blue-50 text-blue-600 rounded text-xs font-medium">{{ institution.level }}</span>
              <span v-if="institution.nature" class="px-2.5 py-0.5 bg-gray-100 text-gray-500 rounded text-xs">{{ natureMap[institution.nature] || institution.nature }}</span>
              <span v-if="institution.provinceCode" class="px-2.5 py-0.5 bg-gray-100 text-gray-500 rounded text-xs">{{ institution.provinceCode }}</span>
            </div>
            <div class="flex gap-4 mt-2 text-xs text-gray-400">
              <span v-if="institution.address">📍 {{ institution.address }}</span>
              <span v-if="institution.officialWebsite">🔗 <a :href="institution.officialWebsite" target="_blank" class="text-blue-500 hover:underline">{{ institution.officialWebsite }}</a></span>
            </div>
          </div>
          <button @click="toggleFav" :class="isFavorited ? 'bg-red-50 text-red-500 border-red-200' : 'bg-white text-gray-400 border-gray-200 hover:border-red-300 hover:text-red-400'"
            class="px-5 py-2.5 border-2 rounded-xl text-sm font-medium transition-colors flex items-center gap-1.5 flex-shrink-0">
            <Icon :icon="isFavorited ? 'mdi:heart' : 'mdi:heart-outline'" class="text-lg" /> {{ isFavorited ? '已收藏' : '收藏院校' }}
          </button>
        </div>

        <!-- 院校简介 -->
        <div v-if="institution.introduction" class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
          <h3 class="font-semibold text-gray-700 mb-3">🏫 院校简介</h3>
          <p class="text-sm text-gray-600 leading-relaxed whitespace-pre-line">{{ institution.introduction }}</p>
        </div>

        <!-- Score trend chart -->
        <div v-if="scores.length" class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
          <div class="flex items-center justify-between mb-4">
            <h3 class="font-semibold text-gray-700">📊 录取分趋势</h3>
            <select v-model="chartMajor" class="text-xs border border-gray-200 rounded-lg px-3 py-1.5 outline-none focus:border-blue-500 bg-white text-gray-600">
              <option value="all">全部专业</option>
              <option v-for="m in chartMajors" :key="m" :value="m">{{ m }}</option>
            </select>
          </div>
          <VChart :option="chartOption" style="height: 300px" autoresize />
        </div>

        <!-- Majors -->
        <div class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div class="flex border-b border-gray-100">
            <button :class="activeTab === 'majors' ? 'text-blue-600 border-blue-600 font-semibold' : 'text-gray-400 border-transparent'"
              class="px-6 py-3.5 text-sm border-b-2 transition-colors" @click="activeTab = 'majors'">📋 招生专业</button>
            <button :class="activeTab === 'scores' ? 'text-blue-600 border-blue-600 font-semibold' : 'text-gray-400 border-transparent'"
              class="px-6 py-3.5 text-sm border-b-2 transition-colors" @click="activeTab = 'scores'">📊 历年录取分</button>
            <button v-if="guides.length" :class="activeTab === 'guides' ? 'text-blue-600 border-blue-600 font-semibold' : 'text-gray-400 border-transparent'"
              class="px-6 py-3.5 text-sm border-b-2 transition-colors" @click="activeTab = 'guides'">📄 招生简章</button>
          </div>

          <div v-if="activeTab === 'majors' && majors.length" class="overflow-x-auto">
            <table class="w-full text-xs">
              <thead><tr class="bg-gray-50 text-gray-500">
                <th class="p-3 text-left">专业代码</th><th class="p-3 text-left">专业名称</th><th class="p-3 text-left">批次</th><th class="p-3 text-left">选科要求</th><th class="p-3 text-center">计划数</th><th class="p-3 text-center">学制</th><th class="p-3 text-right">学费</th>
              </tr></thead>
              <tbody>
                <tr v-for="m in majors" :key="m.id" class="border-t border-gray-50 hover:bg-gray-50/50">
                  <td class="p-3 text-gray-500">{{ m.majorCode }}</td>
                  <td class="p-3 font-medium text-gray-800">{{ m.majorName }}</td>
                  <td class="p-3 text-gray-500">{{ m.batchCode }}</td>
                  <td class="p-3 text-gray-500">{{ m.limitSubjects || '-' }}</td>
                  <td class="p-3 text-center">{{ m.planCount }}</td>
                  <td class="p-3 text-center">{{ m.schoolingLength }}年</td>
                  <td class="p-3 text-right">{{ m.tuitionFee ? m.tuitionFee + '元' : '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="activeTab === 'scores' && scores.length" class="overflow-x-auto">
            <table class="w-full text-xs">
              <thead><tr class="bg-gray-50 text-gray-500">
                <th class="p-3 text-center">年份</th><th class="p-3 text-left">专业</th><th class="p-3 text-center">最低分</th><th class="p-3 text-center">最高分</th><th class="p-3 text-center">平均分</th><th class="p-3 text-center">最低位次</th><th class="p-3 text-center">录取人数</th>
              </tr></thead>
              <tbody>
                <tr v-for="r in scores" :key="r.id" class="border-t border-gray-50 hover:bg-gray-50/50">
                  <td class="p-3 text-center text-gray-500">{{ r.year }}</td>
                  <td class="p-3 font-medium text-gray-800">{{ r.majorName }}</td>
                  <td class="p-3 text-center text-red-500 font-semibold">{{ r.minScore }}</td>
                  <td class="p-3 text-center">{{ r.maxScore }}</td>
                  <td class="p-3 text-center">{{ r.avgScore }}</td>
                  <td class="p-3 text-center">{{ r.minRank || '-' }}</td>
                  <td class="p-3 text-center">{{ r.enrollmentCount }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="activeTab === 'guides' && guides.length" class="p-5 space-y-4">
            <div v-for="(g, idx) in guides" :key="g.id" class="border border-gray-100 rounded-xl p-4 hover:border-blue-200 transition-colors">
              <div class="flex items-center justify-between mb-2">
                <h4 class="font-semibold text-gray-800 text-sm">{{ g.title }}</h4>
                <span class="text-xs text-gray-400 bg-gray-50 px-2 py-0.5 rounded">{{ g.year }}年</span>
              </div>
              <p class="text-xs text-gray-600 leading-relaxed line-clamp-4 whitespace-pre-line">{{ g.content }}</p>
              <div class="flex items-center gap-3 mt-3">
                <button v-if="g.sourceFileUrl" @click="window.open(g.sourceFileUrl, '_blank')" class="text-xs text-blue-500 hover:text-blue-700 flex items-center gap-1">
                  📎 {{ g.sourceFileName || '查看原文件' }}
                </button>
                <button @click="guideTab = guideTab === idx ? -1 : idx" class="text-xs text-gray-400 hover:text-gray-600">
                  {{ guideTab === idx ? '收起' : '展开全文' }}
                </button>
              </div>
              <p v-if="guideTab === idx" class="text-xs text-gray-600 leading-relaxed mt-3 pt-3 border-t border-gray-50 whitespace-pre-line">{{ g.content }}</p>
            </div>
          </div>

          <div v-if="activeTab === 'majors' && !majors.length" class="text-center py-16 text-gray-400 text-sm">暂无招生专业数据</div>
          <div v-if="activeTab === 'scores' && !scores.length" class="text-center py-16 text-gray-400 text-sm">暂无历年录取数据</div>
        </div>
      </div>

      <!-- Right sidebar 30% -->
      <aside class="w-80 flex-shrink-0 space-y-4">
        <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 space-y-3">
          <button @click="router.push('/ai')" class="w-full py-2.5 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors">🤖 AI 分析录取概率</button>
          <button @click="router.push('/favorites')" class="w-full py-2.5 bg-white border border-gray-200 text-gray-600 rounded-lg text-sm hover:bg-gray-50 transition-colors">📊 加入收藏对比</button>
          <button @click="shareInstitution" :class="shareCopied ? 'bg-green-50 border-green-300 text-green-600' : 'bg-white border-gray-200 text-gray-600 hover:bg-gray-50'" class="w-full py-2.5 border rounded-lg text-sm transition-colors">{{ shareCopied ? '✅ 链接已复制' : '📤 分享院校信息' }}</button>
        </div>

        <!-- Contact info -->
        <div v-if="institution.contactPhone || institution.contactEmail" class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
          <h4 class="font-semibold text-gray-700 text-sm mb-3">📞 联系方式</h4>
          <div class="space-y-2 text-xs text-gray-500">
            <div v-if="institution.contactPhone" class="flex items-center gap-2">
              <span class="text-gray-400">📱</span>
              <span>{{ institution.contactPhone }}</span>
            </div>
            <div v-if="institution.contactEmail" class="flex items-center gap-2">
              <span class="text-gray-400">📧</span>
              <span class="text-blue-500">{{ institution.contactEmail }}</span>
            </div>
            <div v-if="institution.address" class="flex items-center gap-2">
              <span class="text-gray-400">📍</span>
              <span>{{ institution.address }}</span>
            </div>
          </div>
        </div>

        <!-- Probability card -->
        <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
          <h4 class="font-semibold text-gray-700 text-sm mb-4">录取概率评估</h4>
          <div class="space-y-4">
            <div>
              <div class="flex justify-between text-xs mb-1"><span class="text-red-500 font-medium">冲刺</span><span class="text-gray-400">23%</span></div>
              <div class="h-2 bg-gray-100 rounded-full overflow-hidden"><div class="h-full bg-red-400 rounded-full" style="width:23%"></div></div>
            </div>
            <div>
              <div class="flex justify-between text-xs mb-1"><span class="text-blue-500 font-medium">稳妥</span><span class="text-gray-400">58%</span></div>
              <div class="h-2 bg-gray-100 rounded-full overflow-hidden"><div class="h-full bg-blue-400 rounded-full" style="width:58%"></div></div>
            </div>
            <div>
              <div class="flex justify-between text-xs mb-1"><span class="text-green-500 font-medium">保底</span><span class="text-gray-400">19%</span></div>
              <div class="h-2 bg-gray-100 rounded-full overflow-hidden"><div class="h-full bg-green-400 rounded-full" style="width:19%"></div></div>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>

  <div v-else-if="loading" class="text-center py-32 text-gray-400">加载中...</div>
  <div v-else class="text-center py-32 text-gray-400">院校不存在</div>
</template>
