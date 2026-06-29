<template>
  <div class="pg">
    <div class="head">
      <div><h1>📊 录取分数管理</h1><p>管理各专业历年录取分数线、位次及人数</p></div>
      <button class="btn" @click="add">+ 新增记录</button>
    </div>

    <div class="tbl">
      <table>
        <thead><tr><th>ID</th><th>专业名称</th><th>年份</th><th>省份</th><th>科类</th><th>最低分</th><th>最高分</th><th>平均分</th><th>最低位次</th><th>录取人数</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="r in list" :key="r.id">
            <td class="dim">{{ r.id }}</td>
            <td class="b">{{ r.majorName }}</td>
            <td>{{ r.year }}</td>
            <td>{{ r.provinceCode }}</td>
            <td>{{ r.categoryCode }}</td>
            <td class="score">{{ r.minScore }}</td>
            <td class="score">{{ r.maxScore }}</td>
            <td class="score">{{ r.avgScore }}</td>
            <td>{{ r.minRank?.toLocaleString() }}</td>
            <td>{{ r.enrollmentCount }}</td>
            <td class="act">
              <button class="sm" @click="edit(r)">编辑</button>
              <button class="sm del" @click="del(r.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!list.length" class="empty">暂无录取分数数据</p>
    </div>

    <div class="pgr">
      <button class="bo" :disabled="pn<=1" @click="pn--;load()">上一页</button>
      <span>第 {{ pn }} 页</span>
      <button class="bo" :disabled="list.length < ps" @click="pn++;load()">下一页</button>
    </div>

    <div v-if="md" class="mask" @click.self="md=false">
      <div class="mod">
        <h2>{{ eid?'编辑':'新增' }}录取分数</h2>
        <form @submit.prevent="save">
          <div class="r2">
            <label>专业名称<b>*</b><input v-model="f.majorName" required /></label>
            <label>年份<b>*</b><input v-model.number="f.year" type="number" required /></label>
          </div>
          <div class="r2">
            <label>省份代码<b>*</b><input v-model="f.provinceCode" required placeholder="如：110000" /></label>
            <label>科类代码<b>*</b><input v-model="f.categoryCode" required placeholder="如：1=理科 2=文科" /></label>
          </div>
          <div class="r3">
            <label>最低分<b>*</b><input v-model.number="f.minScore" type="number" required /></label>
            <label>最高分<b>*</b><input v-model.number="f.maxScore" type="number" required /></label>
            <label>平均分<b>*</b><input v-model.number="f.avgScore" type="number" required /></label>
          </div>
          <div class="r2">
            <label>最低位次<input v-model.number="f.minRank" type="number" /></label>
            <label>录取人数<input v-model.number="f.enrollmentCount" type="number" /></label>
          </div>
          <div class="mbtn">
            <button type="button" class="bo" @click="md=false">取消</button>
            <button type="submit" class="btn">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageList, addScore, updateScore, deleteScore } from '@/api/admissionScore'
// 列表、分页、弹窗控制
const list = ref([]), pn = ref(1), ps = 10, md = ref(false), eid = ref(null)
// 录取分数表单，年份默认当年
const f = reactive({ majorName:'', year:new Date().getFullYear(), provinceCode:'', categoryCode:'', minScore:null, maxScore:null, avgScore:null, minRank:null, enrollmentCount:null })
onMounted(()=>load())
// 取localStorage里的院校ID去查自己的数据
async function load() {
  let iid = localStorage.getItem('instInstitutionId')
  if (!iid || iid === 'null' || iid === 'undefined' || iid === '0') iid = null
  try { list.value = await pageList({ institutionId:iid?Number(iid):undefined, pageNum:pn.value, pageSize:ps }) } catch(e) { console.error('查询失败:', e) }
}
function add() { eid.value=null; f.majorName=''; f.year=new Date().getFullYear(); f.provinceCode=''; f.categoryCode=''; f.minScore=null; f.maxScore=null; f.avgScore=null; f.minRank=null; f.enrollmentCount=null; md.value=true }
// 编辑：把选中行数据直接覆盖进表单
function edit(r) { eid.value=r.id; Object.assign(f,r); md.value=true }
// 保存时从localStorage补齐院校ID
async function save() {
  let iid = localStorage.getItem('instInstitutionId')
  if (iid === 'null' || iid === 'undefined') iid = null
  const d = { ...f, institutionId: (iid && iid !== '0') ? Number(iid) : Number(iid || '0') }
  if (eid.value) d.id = eid.value
  try {
    if (eid.value) await updateScore(d); else await addScore(d)
    md.value = false; load()
  } catch(e) { console.error('保存失败:', e); alert('保存失败：'+(e.response?.data?.message||e.message)) }
}
async function del(id) { if(!confirm('确认删除？'))return; try{await deleteScore(id);load()}catch(e){alert('删除失败')} }
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.head h1 { font-size: 24px; font-weight: 700; color: #1a1a2e; }
.head p { color: #888; font-size: 14px; margin-top: 4px; }
.btn { padding: 9px 22px; background: #0d9488; color: #fff; border: none; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; }
.btn:hover { background: #0f766e; }
.bo { padding: 9px 22px; background: #fff; color: #555; border: 1px solid #d9d9d9; border-radius: 8px; font-size: 14px; cursor: pointer; }
.bo:hover { border-color: #0d9488; color: #0d9488; }
.sm { padding: 4px 10px; font-size: 12px; border-radius: 5px; border: 1px solid #e0e0e0; background: #fff; color: #555; cursor: pointer; margin-right: 4px; }
.sm:hover { border-color: #0d9488; color: #0d9488; }
.sm.del:hover { border-color: #ef4444; color: #ef4444; }
.tbl { background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; overflow-x: auto; }
table { width: 100%; border-collapse: collapse; min-width: 900px; }
th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #f5f5f5; font-size: 13px; white-space: nowrap; }
th { background: #fafafa; color: #888; font-weight: 600; font-size: 12px; }
td { color: #444; }
.dim { color: #bbb; font-size: 12px; }
.b { font-weight: 600; color: #333; }
.score { font-weight: 600; color: #0d9488; }
.empty { text-align: center; color: #bbb; padding: 48px; font-size: 14px; }
.pgr { display: flex; gap: 12px; align-items: center; margin-top: 18px; justify-content: center; font-size: 14px; color: #888; }
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.3); display: flex; align-items: center; justify-content: center; z-index: 100; }
.mod { background: #fff; padding: 32px; border-radius: 14px; width: 620px; max-height: 84vh; overflow-y: auto; box-shadow: 0 16px 48px rgba(0,0,0,.12); }
.mod h2 { margin-bottom: 22px; font-size: 19px; font-weight: 700; color: #1a1a2e; }
label { display: block; margin-bottom: 14px; font-size: 13px; font-weight: 600; color: #555; }
label b { color: #ef4444; margin-left: 2px; }
input { width: 100%; padding: 8px 12px; border: 1px solid #d9d9d9; border-radius: 7px; font-size: 14px; outline: none; box-sizing: border-box; margin-top: 4px; font-family: inherit; }
input:focus { border-color: #0d9488; }
.r2 { display: flex; gap: 14px; }
.r2 label { flex: 1; }
.r3 { display: flex; gap: 14px; }
.r3 label { flex: 1; }
.mbtn { display: flex; gap: 10px; margin-top: 20px; justify-content: flex-end; }
</style>
