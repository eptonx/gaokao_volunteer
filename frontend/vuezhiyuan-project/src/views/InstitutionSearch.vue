<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { filterInstitutions, searchInstitutions, getFilterOptions } from '@/api/institution.js'
import { addFavorite, cancelFavorite, checkFavorite } from '@/api/favorite.js'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const errorMsg = ref('')
const institutions = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const keyword = ref('')

const filters = ref({ provinceCode: '', level: '', nature: '' })
const filterOptions = ref({ province: [], level: [], property: [] })

const natureMap = { 1: '公办', 2: '民办' }
const provinceCodeMap = {
  '北京市': '110000','天津市': '120000','河北省': '130000','山西省': '140000',
  '内蒙古自治区': '150000','辽宁省': '210000','吉林省': '220000','黑龙江省': '230000',
  '上海市': '310000','江苏省': '320000','浙江省': '330000','安徽省': '340000',
  '福建省': '350000','江西省': '360000','山东省': '370000','河南省': '410000',
  '湖北省': '420000','湖南省': '430000','广东省': '440000','广西壮族自治区': '450000',
  '海南省': '460000','重庆市': '500000','四川省': '510000','贵州省': '520000',
  '云南省': '530000','西藏自治区': '540000','陕西省': '610000','甘肃省': '620000',
  '青海省': '630000','宁夏回族自治区': '640000','新疆维吾尔自治区': '650000',
}
const codeToProvince = {}
Object.entries(provinceCodeMap).forEach(([n, c]) => { codeToProvince[c] = n })

onMounted(async () => {
  try { const o = await getFilterOptions(); filterOptions.value = o.data || o } catch {}
  fetchList()
})

async function fetchList() {
  loading.value = true; errorMsg.value = ''
  try {
    const activeParams = {}
    const pc = provinceCodeMap[filters.value.provinceCode]
    if (pc) activeParams.provinceCode = pc
    if (filters.value.level) activeParams.level = filters.value.level
    if (filters.value.nature) activeParams.nature = filters.value.nature

    let res
    if (keyword.value.trim()) res = await searchInstitutions(keyword.value.trim(), pageNum.value, pageSize.value)
    else res = await filterInstitutions(activeParams, pageNum.value, pageSize.value)

    const data = res?.data || {}
    institutions.value = data.rows || []
    total.value = data.total || 0
    // 查询已收藏状态
    try {
      const checks = await Promise.all(institutions.value.map(i => checkFavorite(auth.userId, i.id, 1)))
      faved.value = new Set(institutions.value.filter((_, idx) => (checks[idx]?.data ?? checks[idx]) === true).map(i => i.id))
    } catch {}
  } catch {
    errorMsg.value = '加载失败，请稍后重试'
    institutions.value = []; total.value = 0
  } finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchList() }
function handleReset() { filters.value = { provinceCode: '', level: '', nature: '' }; keyword.value = ''; pageNum.value = 1; fetchList() }
function goDetail(id) { router.push(`/institution/${id}`) }
const totalPages = () => Math.ceil(total.value / pageSize.value) || 1
function goPage(p) { if (p < 1 || p > totalPages()) return; pageNum.value = p; fetchList() }

const visiblePages = () => {
  const total = totalPages()
  const cur = pageNum.value
  const maxShow = 7
  if (total <= maxShow) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }
  // 滑动窗口：当前页尽量居中
  let start = Math.max(1, cur - Math.floor(maxShow / 2))
  let end = start + maxShow - 1
  if (end > total) {
    end = total
    start = Math.max(1, end - maxShow + 1)
  }
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
}

const faved = ref(new Set())
async function toggleFav(inst, e) {
  e.stopPropagation()
  const id = inst.id
  try {
    if (faved.value.has(id)) { await cancelFavorite(auth.userId, id, 1); faved.value.delete(id) }
    else { await addFavorite(auth.userId, id, 1); faved.value.add(id) }
    faved.value = new Set(faved.value)
  } catch {}
}
</script>

<template>
  <div class="flex gap-6 h-[calc(100vh-4rem)] max-w-7xl mx-auto p-4 lg:p-6">
    <!-- Left filter panel -->
    <aside class="w-72 flex-shrink-0 space-y-4">
      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-semibold text-gray-700">筛选条件</h3>
          <button @click="handleReset" class="text-xs text-blue-600 hover:underline">重置</button>
        </div>

        <label class="text-xs text-gray-500 block mb-1.5">院校地区</label>
        <select v-model="filters.provinceCode" @change="handleSearch" class="w-full p-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-blue-500 mb-4">
          <option value="">全部地区</option>
          <option v-for="opt in filterOptions.province" :key="opt" :value="opt">{{ opt }}</option>
        </select>

        <label class="text-xs text-gray-500 block mb-1.5">院校层次</label>
        <div class="flex flex-wrap gap-2 mb-4">
          <button v-for="l in filterOptions.level" :key="l" @click="filters.level = filters.level === l ? '' : l; handleSearch()"
            :class="filters.level === l ? 'bg-blue-600 text-white' : 'bg-gray-50 text-gray-500 hover:bg-gray-100'"
            class="px-3 py-1.5 rounded-full text-xs transition-colors">{{ l }}</button>
        </div>

        <label class="text-xs text-gray-500 block mb-1.5">院校类型</label>
        <div class="flex flex-wrap gap-2 mb-4">
          <button v-for="(label, code) in natureMap" :key="code" @click="filters.nature = filters.nature === code ? '' : code; handleSearch()"
            :class="filters.nature === code ? 'bg-blue-600 text-white' : 'bg-gray-50 text-gray-500 hover:bg-gray-100'"
            class="px-3 py-1.5 rounded-full text-xs transition-colors">{{ label }}</button>
        </div>

        <button @click="handleSearch" class="w-full py-2.5 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors">应用筛选</button>
      </div>
    </aside>

    <!-- Right content -->
    <main class="flex-1 min-w-0">
      <!-- Search bar -->
      <div class="flex gap-3 mb-5">
        <div class="relative flex-1">
          <Icon icon="mdi:magnify" class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input v-model="keyword" type="text" placeholder="搜索院校名称..." @keyup.enter="handleSearch"
            class="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-lg text-sm outline-none focus:border-blue-500 bg-white" />
        </div>
        <button @click="handleSearch" class="px-5 py-2.5 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors flex items-center gap-1">
          <Icon icon="mdi:magnify" /> 搜索
        </button>
      </div>

      <p v-if="total" class="text-xs text-gray-400 mb-4">共 {{ total }} 所院校</p>
      <div v-if="errorMsg" class="text-center py-6 text-red-500 text-sm bg-red-50 rounded-lg mb-4">{{ errorMsg }}</div>
      <div v-if="loading" class="text-center py-20 text-gray-400">加载中...</div>

      <!-- Card grid -->
      <div v-else-if="institutions.length" class="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div v-for="inst in institutions" :key="inst.id" @click="goDetail(inst.id)"
          class="flex gap-4 p-5 bg-white rounded-xl border border-gray-100 shadow-sm cursor-pointer hover:shadow-md hover:-translate-y-0.5 transition-all">
          <img :src="inst.logoUrl || '/favicon.ico'" class="w-14 h-14 rounded-xl object-cover bg-gray-100" @error="e => e.target.style.display='none'" />
          <div class="flex-1 min-w-0">
            <h3 class="font-semibold text-gray-800 text-sm truncate">{{ inst.institutionName }}</h3>
            <div class="flex flex-wrap gap-1.5 mt-1.5">
              <span v-if="inst.level" class="px-2 py-0.5 bg-blue-50 text-blue-600 rounded text-xs">{{ inst.level }}</span>
              <span v-if="inst.nature" class="px-2 py-0.5 bg-gray-100 text-gray-500 rounded text-xs">{{ natureMap[inst.nature] || inst.nature }}</span>
              <span v-if="inst.provinceCode" class="px-2 py-0.5 bg-gray-100 text-gray-500 rounded text-xs">{{ codeToProvince[inst.provinceCode] || inst.provinceCode }}</span>
            </div>
            <p v-if="inst.address" class="text-xs text-gray-400 mt-1.5 truncate">📍 {{ inst.address }}</p>
          </div>
          <button :class="faved.has(inst.id) ? 'text-red-400' : 'text-gray-300 hover:text-red-400'" class="text-lg self-start flex-shrink-0 transition-colors" @click="toggleFav(inst, $event)">
            <Icon :icon="faved.has(inst.id) ? 'mdi:heart' : 'mdi:heart-outline'" />
          </button>
        </div>
      </div>

      <div v-else-if="!loading" class="text-center py-20 text-gray-400">
        <p class="text-4xl mb-3">📭</p>
        <p>暂无匹配院校，试试其他关键词或筛选条件</p>
      </div>

      <!-- Pagination -->
      <div v-if="total > pageSize" class="flex justify-center items-center gap-1.5 mt-8">
        <button :disabled="pageNum <= 1" @click="goPage(1)" class="px-3 py-2 border border-gray-200 rounded-lg text-xs bg-white hover:bg-gray-50 disabled:opacity-40 transition-colors">首页</button>
        <button :disabled="pageNum <= 1" @click="goPage(pageNum - 1)" class="px-3 py-2 border border-gray-200 rounded-lg text-sm bg-white hover:bg-gray-50 disabled:opacity-40 transition-colors">上一页</button>
        <span v-if="visiblePages()[0] > 1" class="px-1 text-gray-400">…</span>
        <button v-for="p in visiblePages()" :key="p" @click="goPage(p)"
          :class="p === pageNum ? 'bg-gray-900 text-white' : 'bg-white text-gray-600 hover:bg-gray-50'"
          class="w-10 h-10 rounded-lg text-sm border border-gray-200 transition-colors">{{ p }}</button>
        <span v-if="visiblePages()[visiblePages().length-1] < totalPages()" class="px-1 text-gray-400">…</span>
        <button :disabled="pageNum >= totalPages()" @click="goPage(pageNum + 1)" class="px-3 py-2 border border-gray-200 rounded-lg text-sm bg-white hover:bg-gray-50 disabled:opacity-40 transition-colors">下一页</button>
        <button :disabled="pageNum >= totalPages()" @click="goPage(totalPages())" class="px-3 py-2 border border-gray-200 rounded-lg text-xs bg-white hover:bg-gray-50 disabled:opacity-40 transition-colors">末页</button>
      </div>
    </main>
  </div>
</template>
