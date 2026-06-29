<template>
  <div class="pg">
    <h1>账号设置</h1>
    <p class="sub">管理院校基本信息、登录密码与资质审核状态</p>
    <div class="cards">
      <div class="pan"><h2>📋 院校基本信息</h2><p class="d">以下信息对外展示在招生简章中</p>
        <form @submit.prevent="sv" class="fm">
          <div class="r">
            <label class="fx">院校名称<b>*</b><input v-model="i.institutionName" name="institutionName" placeholder="请输入院校名称" required /></label>
            <label class="fx">院校代码<input v-model="i.institutionCode" name="institutionCode" placeholder="如：10495" /></label>
          </div>
          <div class="r">
            <label class="fx">Logo（URL或上传）<input v-model="i.logoUrl" name="logoUrl" placeholder="https://... 或点击右侧按钮上传" /></label>
            <label class="fx up"><span>本地上传</span><input type="file" accept="image/*" @change="uploadLogo" /><small v-if="uping">上传中...</small></label>
          </div>
          <div class="r" v-if="i.logoUrl"><div class="preview"><img :src="i.logoUrl" alt="logo预览" @error="e => e.target.style.display='none'" /></div></div>
          <div class="r"><label class="fx">官方网站<input v-model="i.officialWebsite" name="officialWebsite" placeholder="https://..." /></label><label class="fx">联系电话<input v-model="i.contactPhone" name="contactPhone" placeholder="招生咨询电话" /></label></div>
          <label>联系邮箱<input v-model="i.contactEmail" name="contactEmail" placeholder="admission@..." /></label>
          <label>地址<input v-model="i.address" name="address" placeholder="院校详细地址" /></label>
          <label>院校简介<textarea v-model="i.introduction" name="introduction" rows="3" placeholder="简要介绍院校特色与优势"></textarea></label>
          <button type="submit" class="btn">保存基本信息</button>
        </form>
      </div>
      <div class="pan"><h2>🔒 修改密码</h2>
        <form @submit.prevent="cp" class="fm nw">
          <div class="r"><label class="fx">原密码<input v-model="p.old" name="oldPassword" type="password" required /></label><label class="fx">新密码<input v-model="p.nw" name="newPassword" type="password" required /></label></div>
          <label>确认新密码<input v-model="p.cf" name="confirmPassword" type="password" required /></label>
          <p v-if="pm" :class="pok?'ok':'err'">{{ pm }}</p>
          <button type="submit" class="btn">修改密码</button>
        </form>
      </div>
      <div class="pan"><h2>📝 资质审核状态</h2>
        <table v-if="qs.length">
          <thead><tr><th>材料类型</th><th>文件名</th><th>审核状态</th><th>审核意见</th></tr></thead>
          <tbody><tr v-for="q in qs" :key="q.id"><td><span class="tag">{{ MATERIAL_TYPES[q.materialType] || '其他' }}</span></td><td>{{ q.fileName }}</td><td><span :class="q.reviewStatus===1?'ok':q.reviewStatus===2?'fail':'wait'">{{ REVIEW_STATUSES[q.reviewStatus] || '待审核' }}</span></td><td>{{ q.reviewComment||'-' }}</td></tr></tbody>
        </table>
        <p v-else class="empty">暂无资质材料，请先完成注册提交</p>
      </div>
      <div class="pan danger"><h2>⚠️ 注销账户</h2><p class="d">注销后账户将被永久删除，所有数据无法恢复</p><button class="btn-del" @click="destroy">注销账户</button></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getInstitution, updateSelf } from '@/api/institution'
import { changePassword, deleteAccount } from '@/api/institutionUser'
import { getQualifications } from '@/api/qualification'
import { uploadFile } from '@/api/attachment'
import { useRouter } from 'vue-router'

// 各状态码的中文映射
const MATERIAL_TYPES = ['', '办学许可证', '营业执照', '组织机构代码证', '其他资质']
const REVIEW_STATUSES = ['待审核', '已通过', '已驳回']

const i = reactive({ institutionName:'', institutionCode:'', logoUrl:'', officialWebsite:'', contactPhone:'', contactEmail:'', address:'', introduction:'' })
const p = reactive({ old:'', nw:'', cf:'' })
const pm = ref(''), pok = ref(false), qs = ref([]), uping = ref(false)
const router = useRouter()
onMounted(async () => {
  let iid = localStorage.getItem('instInstitutionId')
  if (iid === 'null' || iid === 'undefined' || iid === '0') iid = null
  // 没拿到院校ID就不加载，说明还没创建院校信息
  if (!iid) return
  try {
    const d = await getInstitution(Number(iid))
    Object.assign(i, d)
  } catch(e) {
    console.error('账号设置：加载院校信息失败', e)
  }
  try {
    qs.value = await getQualifications(Number(iid))
  } catch(e) {
    console.error('账号设置：加载资质材料失败', e)
  }
})
async function sv() {
  // 空值保护：id不存在时后端会新建一条院校记录并返回id，同步写回localStorage
  try {
    const idv = localStorage.getItem('instInstitutionId')
    const validId = (idv && idv !== 'null' && idv !== 'undefined' && idv !== '0') ? Number(idv) : null
    const result = await updateSelf({ ...i, id: validId })
    if (!validId && result && result.id) {
      localStorage.setItem('instInstitutionId', result.id)
    }
    // 同时更新本地表单数据
    if (result) Object.assign(i, result)
    alert('保存成功')
  } catch(e) {
    console.error('保存账号信息失败:', e.response?.data || e.message)
    alert('保存失败：' + (e.response?.data?.message || e.message))
  }
}
async function cp() {
  pm.value=''
  if(p.nw!==p.cf){ pm.value='两次密码不一致'; pok.value=false; return }
  try{ await changePassword({id:Number(localStorage.getItem('instUserId')),oldPassword:p.old,newPassword:p.nw}); pm.value='密码修改成功'; pok.value=true; p.old=''; p.nw=''; p.cf='' }catch(e){ pm.value='修改失败，请检查原密码'; pok.value=false }
}
async function uploadLogo(e) {
  const file = e.target.files[0]
  if (!file) return
  uping.value = true
  try {
    const res = await uploadFile(file)
    if (res.success) i.logoUrl = res.fileUrl
    else alert('上传失败：' + (res.message || '未知错误'))
  } catch (err) {
    alert('上传失败')
  } finally {
    uping.value = false
    e.target.value = ''
  }
}
async function destroy() {
  if (!confirm('确认注销账户？此操作不可撤销，所有数据将被永久删除！')) return
  if (!confirm('再次确认：注销后无法恢复，确定继续？')) return
  try {
    await deleteAccount(Number(localStorage.getItem('instUserId')))
    localStorage.removeItem('instUserId')
    localStorage.removeItem('instUsername')
    localStorage.removeItem('instInstitutionId')
    router.push('/login')
  } catch(e) {
    alert('注销失败：' + (e.response?.data?.message || e.message))
  }
}
</script>

<style scoped>
h1 { font-size: 24px; font-weight: 700; color: #1a1a2e; }
.sub { color: #888; font-size: 14px; margin: 4px 0 24px; }
.cards { display: flex; flex-direction: column; gap: 20px; }
.pan { background: #fff; border-radius: 12px; padding: 24px 28px; border: 1px solid #f0f0f0; }
.pan h2 { font-size: 17px; font-weight: 700; color: #1a1a2e; }
.d { color: #aaa; font-size: 13px; margin: 4px 0 18px; }
.fm { max-width: 660px; }
.fm.nw { max-width: 480px; }
.r { display: flex; gap: 14px; }
.fx { flex: 1; }
label { display: block; margin-bottom: 14px; font-size: 13px; font-weight: 600; color: #555; }
input, textarea { width: 100%; padding: 8px 12px; border: 1px solid #d9d9d9; border-radius: 7px; font-size: 14px; outline: none; box-sizing: border-box; margin-top: 4px; font-family: inherit; }
input:focus, textarea:focus { border-color: #2563eb; }
.btn { padding: 9px 24px; background: #2563eb; color: #fff; border: none; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; }
.btn:hover { background: #1d4ed8; }
.ok { color: #059669; font-size: 13px; margin: 8px 0; }
.err { color: #ef4444; font-size: 13px; margin: 8px 0; }
.tag { padding: 2px 10px; background: #f0f5ff; color: #2563eb; border-radius: 4px; font-size: 12px; }
.ok, .fail, .wait { padding: 2px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.ok { background: #f0fdf4; color: #059669; }
.fail { background: #fef2f2; color: #ef4444; }
.wait { background: #fffbeb; color: #d97706; }
table { width: 100%; border-collapse: collapse; margin-top: 16px; }
th, td { padding: 10px 14px; text-align: left; border-bottom: 1px solid #f5f5f5; font-size: 14px; }
th { background: #fafafa; color: #888; font-weight: 600; font-size: 13px; }
td { color: #444; }
.empty { color: #bbb; padding: 32px 0; text-align: center; font-size: 14px; }
.up { display: flex; align-items: flex-end; gap: 8px; padding-bottom: 0; }
.up span {
  display: inline-block; padding: 9px 14px; background: #f0f5ff; color: #2563eb;
  border: 1px dashed #2563eb; border-radius: 7px; font-size: 13px; cursor: pointer; white-space: nowrap;
  line-height: 1.4;
}
.up input[type=file] { display: none; }
.up small { color: #888; white-space: nowrap; }
.preview { width: 100%; margin-bottom: 4px; }
.preview img { max-width: 120px; max-height: 120px; border-radius: 8px; border: 1px solid #e8e8e8; object-fit: cover; }
.danger { border-color: #fecaca !important; background: #fef2f2 !important; }
.danger h2 { color: #dc2626; }
.btn-del {
  padding: 10px 28px; background: #fff; color: #dc2626; border: 1px solid #dc2626;
  border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer;
}
.btn-del:hover { background: #dc2626; color: #fff; }
</style>
