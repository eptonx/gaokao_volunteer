<template>
  <div class="pg">
    <div class="head">
      <div><h1>招生计划管理</h1><p>管理各年度招生计划，支持单条录入与批量导入</p></div>
      <div class="hbtn">
        <label class="bo">📥 批量导入<input type="file" accept=".json" @change="imp" hidden /></label>
        <button class="btn" @click="add">+ 新增计划</button>
      </div>
    </div>
    <div class="bar"><input v-model="yf" name="year" type="number" placeholder="按年份筛选" @keyup.enter="load" /><button class="bo" @click="load">查询</button></div>
    <div class="tbl">
      <table>
        <thead><tr><th>ID</th><th>年份</th><th>专业名称</th><th>专业方向</th><th>计划人数</th><th>学费(元)</th><th>学制</th><th>选科要求</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="p in list" :key="p.id">
            <td class="dim">{{ p.id }}</td><td>{{ p.year }}</td><td class="b">{{ p.majorName }}</td><td>{{ p.majorDirection||'-' }}</td>
            <td>{{ p.planCount }}</td><td>{{ p.tuitionFee||'-' }}</td><td>{{ p.schoolingLength?p.schoolingLength+'年':'-' }}</td>
            <td><span v-if="p.limitSubjects" class="tag">{{ p.limitSubjects }}</span><span v-else>-</span></td>
            <td><span :class="p.status===1?'s on':'s'">{{ p.status===1?'已发布':'草稿' }}</span></td>
            <td class="act">
              <button class="sm" @click="edit(p)">编辑</button>
              <button class="sm" @click="pub(p)">{{ p.status===1?'撤回':'发布' }}</button>
              <button class="sm del" @click="del(p.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!list.length" class="empty">暂无招生计划数据</p>
    </div>
    <div class="pgr"><button class="bo" :disabled="pn<=1" @click="pn--;load()">上一页</button><span>第 {{ pn }} 页</span><button class="bo" :disabled="list.length < ps" @click="pn++;load()">下一页</button></div>

    <div v-if="md" class="mask" @click.self="md=false">
      <div class="mod"><h2>{{ eid?'编辑':'新增' }}招生计划</h2>
        <form @submit.prevent="save">
          <div class="g"><label>年份<b>*</b><input v-model="f.year" name="year" type="number" required /></label><label>专业代码<input v-model="f.majorCode" name="majorCode" /></label><label>专业名称<b>*</b><input v-model="f.majorName" name="majorName" required /></label><label>专业方向<input v-model="f.majorDirection" name="majorDirection" /></label><label>计划人数<b>*</b><input v-model="f.planCount" name="planCount" type="number" required /></label><label>学费<input v-model="f.tuitionFee" name="tuitionFee" /></label><label>学制(年)<input v-model="f.schoolingLength" name="schoolingLength" type="number" /></label><label>学位类型<input v-model="f.degreeType" name="degreeType" /></label><label>省份代码<input v-model="f.provinceCode" name="provinceCode" /></label><label>批次代码<input v-model="f.batchCode" name="batchCode" /></label><label>科类代码<input v-model="f.categoryCode" name="categoryCode" /></label><label>学历层次<input v-model="f.educationLevel" name="educationLevel" type="number" /></label><label class="s2">选科要求<input v-model="f.limitSubjects" name="limitSubjects" /></label><label class="s2">备注<input v-model="f.remark" name="remark" /></label></div>
          <div class="mbtn"><button type="button" class="bo" @click="md=false">取消</button><button type="submit" class="btn">保存</button></div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageList, addPlan, updatePlan, deletePlan, publishPlan, batchAdd } from '@/api/enrollmentPlan'
const list = ref([]), pn = ref(1), ps = 10, yf = ref(''), md = ref(false), eid = ref(null)
// 招生计划表单，给一些常用默认值方便填写
const f = reactive({ year:new Date().getFullYear(), majorCode:'', majorName:'', majorDirection:'', planCount:null, tuitionFee:null, schoolingLength:null, degreeType:'', provinceCode:'', batchCode:'', categoryCode:'', educationLevel:null, limitSubjects:'', remark:'' })
onMounted(()=>load())
// 按院校和年份筛选，带分页
async function load() {
  let iid = localStorage.getItem('instInstitutionId')
  if (!iid || iid === 'null' || iid === 'undefined' || iid === '0') iid = null
  try {
    const data = await pageList({ institutionId:iid?Number(iid):undefined, year:yf.value||undefined, pageNum:pn.value, pageSize:ps })
    list.value = data
  } catch(e) {
    console.error('招生计划查询失败:', e)
  }
}
// 新增时把表单恢复成默认值
function add() {
  eid.value=null
  f.year=new Date().getFullYear(); f.majorCode='080901'; f.majorName=''; f.majorDirection=''
  f.planCount=100; f.tuitionFee=5000; f.schoolingLength=4; f.degreeType='工学'
  f.provinceCode='11'; f.batchCode='1'; f.categoryCode='5'; f.educationLevel=1
  f.limitSubjects='物理,化学'; f.remark=''
  md.value=true
}
// 编辑：直接把行数据覆盖进表单
function edit(p) { eid.value=p.id; Object.assign(f,p); md.value=true }
// 新增或更新计划，补齐院校ID
async function save() {
  let iid = localStorage.getItem('instInstitutionId')
  if (iid === 'null' || iid === 'undefined') iid = null
  const d = {...f, institutionId:(iid && iid !== '0') ? Number(iid) : Number(iid || '0')}
  const isEdit = !!eid.value
  if (isEdit) d.id = eid.value
  if (!isEdit) {
    d.isNewMajor = 0
    d.status = 0
    d.createdBy = 0
  }
  try {
    if (isEdit) await updatePlan(d); else await addPlan(d)
    md.value = false; load()
  } catch(e) {
    console.error('保存失败:', e.response?.data || e.message)
    alert('保存失败：'+(e.response?.data?.message||e.message))
  }
}
async function pub(p) { try { await publishPlan({id:p.id,status:p.status===1?0:1}); load() } catch(e){alert('操作失败')} }
async function del(id) { if(!confirm('确认删除？'))return; try{await deletePlan(id);load()}catch(e){alert('删除失败')} }
async function imp(e) {
  const file = e.target.files[0]; if(!file)return
  try { const arr = JSON.parse(await file.text()); if(!Array.isArray(arr))throw new Error('需要JSON数组'); let iid2=localStorage.getItem('instInstitutionId'); if (iid2==='null'||iid2==='undefined') iid2=null; arr.forEach(p=>{p.institutionId=iid2?Number(iid2):null}); await batchAdd(arr); alert('成功导入 '+arr.length+' 条'); load() } catch(err){alert('导入失败：'+err.message)} finally { e.target.value = '' }
}
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.head h1 { font-size: 24px; font-weight: 700; color: #1a1a2e; }
.head p { color: #888; font-size: 14px; margin-top: 4px; }
.hbtn { display: flex; gap: 10px; }
.bar { display: flex; gap: 10px; margin-bottom: 16px; }
.bar input { padding: 8px 14px; border: 1px solid #d9d9d9; border-radius: 8px; font-size: 14px; outline: none; width: 140px; }
.bar input:focus { border-color: #2563eb; }
.btn { padding: 9px 22px; background: #2563eb; color: #fff; border: none; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; }
.btn:hover { background: #1d4ed8; }
.bo { padding: 9px 22px; background: #fff; color: #555; border: 1px solid #d9d9d9; border-radius: 8px; font-size: 14px; cursor: pointer; }
.bo:hover { border-color: #2563eb; color: #2563eb; }
.sm { padding: 4px 10px; font-size: 12px; border-radius: 5px; border: 1px solid #e0e0e0; background: #fff; color: #555; cursor: pointer; margin-right: 4px; }
.sm:hover { border-color: #2563eb; color: #2563eb; }
.sm.del:hover { border-color: #ef4444; color: #ef4444; }
.tbl { background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; overflow: hidden; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 10px 14px; text-align: left; border-bottom: 1px solid #f5f5f5; font-size: 13px; }
th { background: #fafafa; color: #888; font-weight: 600; font-size: 12px; }
td { color: #444; }
.dim { color: #bbb; font-size: 12px; }
.b { font-weight: 600; color: #333; }
.tag { padding: 2px 8px; background: #f0f5ff; color: #2563eb; border-radius: 4px; font-size: 12px; }
.s { padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; background: #f5f5f5; color: #999; }
.s.on { background: #f0fdf4; color: #059669; }
.act { white-space: nowrap; }
.empty { text-align: center; color: #bbb; padding: 48px; font-size: 14px; }
.pgr { display: flex; gap: 12px; align-items: center; margin-top: 18px; justify-content: center; font-size: 14px; color: #888; }
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.3); display: flex; align-items: center; justify-content: center; z-index: 100; }
.mod { background: #fff; padding: 32px; border-radius: 14px; width: 660px; max-height: 84vh; overflow-y: auto; box-shadow: 0 16px 48px rgba(0,0,0,.12); }
.mod h2 { margin-bottom: 22px; font-size: 19px; font-weight: 700; color: #1a1a2e; }
.g { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.s2 { grid-column: span 2; }
label { display: block; font-size: 13px; font-weight: 600; color: #555; }
label b { color: #ef4444; margin-left: 2px; }
input { width: 100%; padding: 8px 12px; border: 1px solid #d9d9d9; border-radius: 7px; font-size: 14px; outline: none; box-sizing: border-box; margin-top: 4px; }
input:focus { border-color: #2563eb; }
.mbtn { display: flex; gap: 10px; margin-top: 22px; justify-content: flex-end; }
</style>
