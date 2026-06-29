<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getScores, addScore, updateScore, rankToScore } from '@/api/score'

const auth = useAuthStore()
const scores = ref([])
const editingId = ref(null)
const saving = ref(false)
const eqResult = ref(null)

const years = [2026, 2025, 2024, 2023]
const provinces = [
  { code: '110000', name: '北京' },{ code: '310000', name: '上海' },{ code: '440000', name: '广东' },
  { code: '320000', name: '江苏' },{ code: '330000', name: '浙江' },{ code: '370000', name: '山东' },
  { code: '410000', name: '河南' },{ code: '510000', name: '四川' },{ code: '420000', name: '湖北' },
  { code: '430000', name: '湖南' },{ code: '130000', name: '河北' },{ code: '340000', name: '安徽' },
  { code: '500000', name: '重庆' },{ code: '210000', name: '辽宁' },{ code: '230000', name: '黑龙江' },
]
const subjects = ['物理', '化学', '生物', '历史', '地理', '政治']

// 根据首选科目过滤再选科目：3+1+2 模式下，物理/历史为首选，再选科目从剩余4科中选
const availableSubjects = computed(() => {
  return subjects.filter(s => s !== '物理' && s !== '历史')
})

const form = reactive({ examYear: 2026, provinceCode: '420000', subjectType: 1, totalScore: null, provinceRank: null })

// 切换首选科目时清空已选的再选科目
watch(() => form.subjectType, () => {
  selectedSubjects.value = []
})
const eqForm = reactive({ targetYear: 2024 })
const eqSourceIdx = ref(0) // 用哪条已存成绩做换算
const selectedSubjects = ref([])

function toggleSubject(s) {
  const i = selectedSubjects.value.indexOf(s)
  if (i >= 0) {
    selectedSubjects.value.splice(i, 1)
  } else if (selectedSubjects.value.length < 2) {
    selectedSubjects.value.push(s)
  }
}

async function loadScores() {
  const res = await getScores(auth.userId)
  scores.value = (res.data || res) || []
}

async function save() {
  if (selectedSubjects.value.length < 2) {
    alert('请至少选择2门再选科目')
    return
  }
  saving.value = true
  const data = { ...form, selectedSubjects: selectedSubjects.value.join(','), userId: auth.userId }
  try {
    if (editingId.value) await updateScore({ ...data, id: editingId.value })
    else await addScore(data)
    resetForm()
    await loadScores()
  } finally { saving.value = false }
}

function editScore(s) {
  editingId.value = s.id
  Object.assign(form, { examYear: s.examYear, provinceCode: s.provinceCode, subjectType: s.subjectType, totalScore: s.totalScore, provinceRank: s.provinceRank })
  selectedSubjects.value = s.selectedSubjects ? s.selectedSubjects.split(',') : []
}

function resetForm() {
  editingId.value = null
  Object.assign(form, { examYear: 2026, provinceCode: '420000', subjectType: 1, totalScore: null, provinceRank: null })
  selectedSubjects.value = []
}

async function doEquivalent() {
  const src = scores.value[eqSourceIdx.value]
  if (!src) { eqResult.value = '暂无已保存的成绩'; return }
  if (!src.provinceRank) { eqResult.value = '当前成绩未填写位次'; return }
  try {
    const res = await rankToScore({
      provinceCode: src.provinceCode,
      subjectType: src.subjectType || 1,
      rank: src.provinceRank,
      targetYear: eqForm.targetYear,
    })
    eqResult.value = (res.data ?? res)
  } catch (e) {
    eqResult.value = e.response?.data?.msg || '换算失败'
  }
}

// 无成绩时禁止跳转其他页面
onBeforeRouteLeave((_to, _from, next) => {
  if (scores.value.length === 0) {
    alert('请先录入成绩信息后再访问其他页面')
    next(false)
  } else {
    next()
  }
})

onMounted(loadScores)
</script>

<template>
  <div class="h-[calc(100vh-4rem)] flex flex-col p-4 lg:p-6 max-w-7xl mx-auto">
    <!-- 标题栏 -->
    <div class="flex items-center justify-between mb-4 lg:mb-6">
      <div class="flex items-center gap-2">
        <span class="text-2xl">🎓</span>
        <h1 class="text-xl font-bold text-gray-800">高考成绩管理</h1>
      </div>
      <span class="text-xs text-gray-400 hidden sm:block">精准录入 · 智能换算</span>
    </div>

    <!-- 主体：左右两栏（桌面端） -->
    <div class="flex-1 flex flex-col lg:flex-row gap-4 lg:gap-6 min-h-0">
      <!-- 左栏：表单（固定宽度） -->
      <div class="lg:w-96 flex-shrink-0 glass-card rounded-2xl p-4 lg:p-5 flex flex-col">
        <div class="flex items-center gap-2 mb-4">
          <span class="text-lg">{{ editingId ? '✏️' : '➕' }}</span>
          <h2 class="text-sm font-semibold text-gray-700">{{ editingId ? '修改成绩' : '录入新成绩' }}</h2>
        </div>

        <!-- 表单内容 -->
        <div class="flex-1 space-y-3 overflow-y-auto pr-1">
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-xs text-gray-500 mb-1 block">考试年份</label>
              <select v-model.number="form.examYear" class="w-full p-2 border border-gray-200 rounded-lg text-xs outline-none bg-white/80 focus:border-indigo-300 focus:shadow-[0_0_0_3px_rgba(99,102,241,0.1)]">
                <option v-for="y in years" :key="y" :value="y">{{ y }}年</option>
              </select>
            </div>
            <div>
              <label class="text-xs text-gray-500 mb-1 block">高考省份</label>
              <select v-model="form.provinceCode" class="w-full p-2 border border-gray-200 rounded-lg text-xs outline-none bg-white/80 focus:border-indigo-300 focus:shadow-[0_0_0_3px_rgba(99,102,241,0.1)]">
                <option v-for="p in provinces" :key="p.code" :value="p.code">{{ p.name }}</option>
              </select>
            </div>
          </div>

          <div>
            <label class="text-xs text-gray-500 mb-1 block">首选科目</label>
            <div class="flex gap-2">
              <button @click="form.subjectType = 1" :class="form.subjectType === 1 ? 'bg-blue-500 text-white border-blue-500' : 'bg-white/80 border-gray-200 text-gray-600'" class="flex-1 py-1.5 border rounded-lg text-xs font-medium transition">物理类</button>
              <button @click="form.subjectType = 2" :class="form.subjectType === 2 ? 'bg-amber-500 text-white border-amber-500' : 'bg-white/80 border-gray-200 text-gray-600'" class="flex-1 py-1.5 border rounded-lg text-xs font-medium transition">历史类</button>
            </div>
          </div>

          <div>
            <label class="text-xs text-gray-500 mb-1 block">再选科目（多选）</label>
            <div class="flex flex-wrap gap-1.5">
              <button v-for="s in availableSubjects" :key="s" @click="toggleSubject(s)" :class="selectedSubjects.includes(s) ? 'chip-active' : 'bg-white/80 border-gray-200 text-gray-500'" class="px-2.5 py-1 border rounded-full text-xs transition">{{ s }}</button>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-xs text-gray-500 mb-1 block">高考总分</label>
              <input v-model.number="form.totalScore" type="number" placeholder="例：620" class="w-full p-2 border border-gray-200 rounded-lg text-xs outline-none bg-white/80 focus:border-indigo-300 focus:shadow-[0_0_0_3px_rgba(99,102,241,0.1)]">
            </div>
            <div>
              <label class="text-xs text-gray-500 mb-1 block">全省位次</label>
              <input v-model.number="form.provinceRank" type="number" placeholder="例：15000" class="w-full p-2 border border-gray-200 rounded-lg text-xs outline-none bg-white/80 focus:border-indigo-300 focus:shadow-[0_0_0_3px_rgba(99,102,241,0.1)]">
            </div>
          </div>
        </div>

        <div class="flex gap-2 mt-4 pt-3 border-t border-gray-100">
          <button @click="save" :disabled="saving" class="flex-1 py-2 bg-gray-800 text-white rounded-lg text-xs font-semibold hover:bg-gray-700 disabled:opacity-50 transition">
            {{ saving ? '保存中...' : (editingId ? '更新' : '保存') }}
          </button>
          <button v-if="editingId" @click="resetForm" class="px-4 py-2 text-xs text-gray-500 bg-gray-100 rounded-lg hover:bg-gray-200 transition">取消</button>
        </div>
      </div>

      <!-- 右栏：成绩列表 + 换算 -->
      <div class="flex-1 flex flex-col gap-4 lg:gap-5 min-h-0">
        <!-- 已录入成绩（可滚动列表） -->
        <div class="glass-card rounded-2xl p-4 lg:p-5 flex flex-col min-h-0" style="max-height: 55%;">
          <div class="flex items-center gap-2 mb-3">
            <span class="text-base">📋</span>
            <h2 class="text-sm font-semibold text-gray-700">已录入成绩</h2>
            <span class="text-xs text-gray-400 ml-auto">{{ scores.length }} 条</span>
          </div>
          <div class="flex-1 overflow-y-auto space-y-2 pr-1" v-if="scores.length">
            <div v-for="s in scores" :key="s.id" @click="editScore(s)" class="score-row flex items-center justify-between p-2.5 rounded-xl cursor-pointer border border-gray-100/60 bg-white/50">
              <div class="flex items-center gap-3">
                <span class="text-sm font-bold text-gray-500 w-10">{{ s.examYear }}</span>
                <div>
                  <div class="text-xs font-medium text-gray-700">{{ provinces.find(p => p.code === s.provinceCode)?.name || s.provinceCode }}
                    <span :class="s.subjectType === 1 ? 'text-blue-600 bg-blue-50' : 'text-amber-600 bg-amber-50'" class="ml-1 px-1.5 py-0.5 rounded text-[10px]">{{ s.subjectType === 1 ? '物理' : '历史' }}</span>
                  </div>
                  <div class="text-[10px] text-gray-400 mt-0.5">{{ s.selectedSubjects?.replace(/,/g, '、') || '未选' }}</div>
                </div>
              </div>
              <div class="text-right">
                <div class="text-lg font-bold text-gray-800">{{ s.totalScore }}<span class="text-xs font-normal text-gray-400">分</span></div>
                <div class="text-[10px] text-gray-400">位次{{ s.provinceRank?.toLocaleString() }}</div>
              </div>
            </div>
          </div>
          <div v-else class="text-center text-xs text-gray-400 py-4">暂无成绩，请在左侧添加</div>
        </div>

        <!-- 智能等位分换算 -->
        <div class="glass-card rounded-2xl p-4 lg:p-5 flex-shrink-0">
          <div class="flex items-center gap-2 mb-3">
            <span class="text-base">🧮</span>
            <h2 class="text-sm font-semibold text-gray-700">智能等位分换算</h2>
          </div>

          <div class="flex flex-wrap items-center gap-3">
            <div class="flex items-center gap-2">
              <span class="text-xs text-gray-500">源成绩：</span>
              <select v-model="eqSourceIdx" class="p-1.5 border border-gray-200 rounded-lg text-xs outline-none bg-white/80 max-w-[200px]">
                <option v-for="(s, i) in scores" :key="s.id" :value="i">{{ s.examYear }}年 {{ provinces.find(p => p.code === s.provinceCode)?.name || s.provinceCode }} ({{ s.totalScore }}分)</option>
              </select>
            </div>
            <span class="text-xs text-gray-400">→</span>
            <div class="flex items-center gap-2">
              <span class="text-xs text-gray-500">目标年：</span>
              <select v-model.number="eqForm.targetYear" class="p-1.5 border border-gray-200 rounded-lg text-xs outline-none bg-white/80">
                <option v-for="y in years" :key="y" :value="y">{{ y }}年</option>
              </select>
            </div>
            <button @click="doEquivalent" class="px-4 py-1.5 bg-violet-600 text-white rounded-lg text-xs font-medium hover:bg-violet-700 transition">换算</button>
          </div>

          <div v-if="eqResult !== null" class="eq-badge mt-3 flex items-center gap-3 bg-violet-50 rounded-xl p-3 border border-violet-100">
            <span class="text-lg">🎯</span>
            <div>
              <span class="text-xs text-violet-500">等效分数</span>
              <span class="text-xl font-bold text-violet-700 ml-1">{{ eqResult }}<span class="text-xs font-normal text-violet-400">分</span></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px -1px rgba(0, 0, 0, 0.02);
}

.score-row {
  transition: all 0.2s ease;
}

.score-row:hover {
  background: rgba(249, 250, 251, 0.9);
  transform: translateX(2px);
}

.chip-active {
  background: linear-gradient(135deg, #6366f1, #7c3aed);
  color: white;
  box-shadow: 0 2px 4px rgba(99, 102, 241, 0.3);
}

.eq-badge {
  animation: pop 0.3s ease-out;
}

@keyframes pop {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
</style>
