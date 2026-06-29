<template>
  <div class="pg">
    <div class="head"><div><h1>招生简章管理</h1><p>编辑发布招生简章，支持附件上传与发布管理</p></div><button class="btn" @click="add">+ 新增简章</button></div>
    <div class="tbl">
      <table>
        <thead><tr><th>ID</th><th>标题</th><th>年份</th><th>附件</th><th>发布状态</th><th>浏览量</th><th>下载量</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="g in list" :key="g.id">
            <td class="dim">{{ g.id }}</td><td class="b">{{ g.title }}</td><td>{{ g.year }}</td>
            <td><a v-if="g.sourceFileUrl" :href="g.sourceFileUrl" target="_blank" class="lk">查看</a><span v-else class="na">-</span></td>
            <td><span :class="g.publishStatus===1?'s on':'s'">{{ g.publishStatus===1?'已发布':'草稿' }}</span></td>
            <td>{{ g.viewCount||0 }}</td><td>{{ g.downloadCount||0 }}</td>
            <td class="act">
              <button class="sm" @click="edit(g)">编辑</button>
              <button class="sm" @click="pub(g)">{{ g.publishStatus===1?'撤回':'发布' }}</button>
<button class="sm del" @click="del(g.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="!list.length" class="empty">暂无招生简章数据</p>
    </div>
    <div class="pgr"><button class="bo" :disabled="pn<=1" @click="pn--;load()">上一页</button><span>第 {{ pn }} 页</span><button class="bo" :disabled="list.length < ps" @click="pn++;load()">下一页</button></div>

    <div v-if="md" class="mask" @click.self="md=false">
      <div class="mod"><h2>{{ eid?'编辑':'新增' }}招生简章</h2>
        <form @submit.prevent="save">
          <label>标题<b>*</b><input v-model="f.title" name="title" required /></label>
          <label>年份<b>*</b><input v-model="f.year" name="year" type="number" required /></label>
          <label>简章内容（招生政策、报考条件等）<textarea v-model="f.content" name="content" rows="5" placeholder="请详细描述招生政策、报考条件、专业介绍等内容"></textarea></label>
          <div class="r">
            <label class="fx">附件文件名<input v-model="f.sourceFileName" name="sourceFileName" placeholder="如：2026招生简章.pdf" /></label>
            <label class="fx">附件URL &nbsp;<span class="up-link" @click="uploadFile">📎 上传</span><input v-model="f.sourceFileUrl" name="sourceFileUrl" placeholder="上传后填入文件URL" /><input type="file" ref="fileInput" accept=".pdf,.doc,.docx" @change="doUpload" hidden /></label>
          </div>
          <div class="mbtn"><button type="button" class="bo" @click="md=false">取消</button><button type="submit" class="btn">保存</button></div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageList, addGuide, updateGuide, deleteGuide, publishGuide } from '@/api/admissionGuide'
import { uploadFile as upload } from '@/api/attachment'
const list = ref([]), pn = ref(1), ps = 10, md = ref(false), eid = ref(null), fileInput = ref(null)
// 简章表单字段
const f = reactive({ title:'', year:new Date().getFullYear(), content:'', sourceFileName:'', sourceFileUrl:'' })
onMounted(()=>load())
// 拉当前院校的简章列表，带分页
async function load() {
  let iid = localStorage.getItem('instInstitutionId')
  if (!iid || iid === 'null' || iid === 'undefined' || iid === '0') iid = null
  try {
    const data = await pageList({ institutionId:iid?Number(iid):undefined, pageNum:pn.value, pageSize:ps })
    list.value = data
  } catch(e) {
    console.error('招生简章查询失败:', e)
  }
}
// 新增时清空表单；编辑时拿行数据填进去
function add() { eid.value=null; f.title=''; f.year=new Date().getFullYear(); f.content=''; f.sourceFileName=''; f.sourceFileUrl=''; md.value=true }
// 编辑：把行数据灌进表单
function edit(g) { eid.value=g.id; Object.assign(f,g); md.value=true }
// 新增/保存简章，自动补齐院校ID和初始状态
async function save() {
  let iid = localStorage.getItem('instInstitutionId')
  if (iid === 'null' || iid === 'undefined') iid = null
  const d = {...f, institutionId:(iid && iid !== '0') ? Number(iid) : Number(iid || '0')}
  const isEdit = !!eid.value
  if (isEdit) d.id = eid.value
  if (!isEdit) {
    d.reviewStatus = 0
    d.publishStatus = 0
    d.viewCount = 0
    d.downloadCount = 0
    d.createdBy = 0
  }
  try {
    if (isEdit) await updateGuide(d); else await addGuide(d)
    md.value = false; load()
  } catch(e) {
    console.error('保存失败:', e.response?.data || e.message)
    alert('保存失败：'+(e.response?.data?.message||e.message))
  }
}
async function pub(g) { try { await publishGuide({id:g.id,publishStatus:g.publishStatus===1?0:1}); load() } catch(e){alert('操作失败')} }
async function del(id) { if(!confirm('确认删除？'))return; try{await deleteGuide(id);load()}catch(e){alert('删除失败')} }

function uploadFile() { fileInput.value.click() }
async function doUpload(e) {
  const file = e.target.files[0]
  if (!file) return
  try {
    const res = await upload(file)
    if (res.success) { f.sourceFileName = res.fileName; f.sourceFileUrl = res.fileUrl }
    else alert('上传失败')
  } catch (err) { alert('上传失败') }
  finally { e.target.value = '' }
}
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.head h1 { font-size: 24px; font-weight: 700; color: #1a1a2e; }
.head p { color: #888; font-size: 14px; margin-top: 4px; }
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
.lk { color: #2563eb; text-decoration: none; font-weight: 500; }
.na { color: #ddd; }
.s { padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; background: #f5f5f5; color: #999; }
.s.on { background: #f0fdf4; color: #059669; }
.act { white-space: nowrap; }
.empty { text-align: center; color: #bbb; padding: 48px; font-size: 14px; }
.pgr { display: flex; gap: 12px; align-items: center; margin-top: 18px; justify-content: center; font-size: 14px; color: #888; }
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.3); display: flex; align-items: center; justify-content: center; z-index: 100; }
.mod { background: #fff; padding: 32px; border-radius: 14px; width: 580px; max-height: 84vh; overflow-y: auto; box-shadow: 0 16px 48px rgba(0,0,0,.12); }
.mod h2 { margin-bottom: 22px; font-size: 19px; font-weight: 700; color: #1a1a2e; }
label { display: block; margin-bottom: 14px; font-size: 13px; font-weight: 600; color: #555; }
label b { color: #ef4444; margin-left: 2px; }
input, textarea { width: 100%; padding: 8px 12px; border: 1px solid #d9d9d9; border-radius: 7px; font-size: 14px; outline: none; box-sizing: border-box; margin-top: 4px; font-family: inherit; }
input:focus, textarea:focus { border-color: #2563eb; }
.r { display: flex; gap: 14px; }
.fx { flex: 1; }
.mbtn { display: flex; gap: 10px; margin-top: 20px; justify-content: flex-end; }
.up-link { color: #2563eb; cursor: pointer; font-size: 12px; font-weight: 600; white-space: nowrap; }
.up-link:hover { text-decoration: underline; }
</style>
