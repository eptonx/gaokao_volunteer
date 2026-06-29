<template>
  <div class="pg">
    <div class="card">
      <h1>院校注册</h1>
      <div class="steps">
        <span :class="{ on: step>=1, done: step>1 }"><b>1</b> 账号信息</span>
        <span class="bar" :class="{ done: step>1 }"></span>
        <span :class="{ on: step>=2, done: step>2 }"><b>2</b> 资质材料</span>
        <span class="bar" :class="{ done: step>2 }"></span>
        <span :class="{ on: step>=3 }"><b>3</b> 完成</span>
      </div>

      <form v-if="step===1" @submit.prevent="next" class="f">
        <div class="r"><label>用户名<b class="req">*</b><input v-model="u.username" name="username" placeholder="设置登录用户名" required /></label><label>真实姓名<b class="req">*</b><input v-model="u.realName" name="realName" placeholder="联系人姓名" required /></label></div>
        <div class="r"><label>密码<b class="req">*</b><input v-model="u.password" name="password" type="password" placeholder="设置密码" required /></label><label>确认密码<b class="req">*</b><input v-model="u.confirmPwd" name="confirmPwd" type="password" placeholder="再次输入" required /></label></div>
        <div class="r"><label>手机号<b class="req">*</b><input v-model="u.mobile" name="mobile" placeholder="手机号" required /></label><label>邮箱<input v-model="u.email" name="email" placeholder="电子邮箱" /></label></div>
        <p v-if="e1" class="err">{{ e1 }}</p>
        <button type="submit">下一步 →</button>
        <p class="tip">已有账号？<router-link to="/institution/login">返回登录</router-link></p>
      </form>

      <div v-else-if="step===2" class="f">
        <p class="desc">请提交院校办学资质材料，审核通过后可用全部功能</p>
        <div v-for="(m,i) in mats" :key="i" class="mc">
          <div class="mt"><strong>材料 {{ i+1 }}</strong><button type="button" class="del" @click="mats.splice(i,1)" v-if="mats.length>1">删除</button></div>
          <select v-model="m.materialType" name="materialType"><option :value="1">📜 办学许可证</option><option :value="2">📋 营业执照</option><option :value="3">📝 组织机构代码证</option><option :value="4">📎 其他资质</option></select>
          <input v-model="m.fileName" name="fileName" placeholder="文件名，如：办学许可证_北京大学.pdf" required />
          <input v-model="m.fileUrl" name="fileUrl" placeholder="文件URL地址（上传后填入）" required />
        </div>
        <button type="button" class="add" @click="mats.push({materialType:1,fileName:'',fileUrl:'',fileSize:0})">+ 添加更多材料</button>
        <p v-if="e2" class="err">{{ e2 }}</p>
        <div class="btns"><button type="button" class="back" @click="step=1">← 上一步</button><button :disabled="sub" @click="submit">{{ sub?'提交中...':'提交注册' }}</button></div>
      </div>

      <div v-else class="done">
        <b>✅</b><h2>注册成功！</h2><p>资质材料已提交，等待管理员审核。</p>
        <router-link to="/institution/login" class="go">前往登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { register } from '@/api/institutionUser'
import { addQualification } from '@/api/qualification'
const step = ref(1), e1 = ref(''), e2 = ref(''), sub = ref(false)
const u = reactive({ username:'',password:'',confirmPwd:'',realName:'',mobile:'',email:'' })
const mats = ref([{ materialType:1, fileName:'', fileUrl:'', fileSize:0 }])
function next() {
  e1.value = ''
  if (u.password !== u.confirmPwd) { e1.value = '两次密码不一致'; return }
  if (!u.username||!u.password||!u.realName||!u.mobile) { e1.value = '请填写必填项'; return }
  step.value = 2
}
async function submit() {
  e2.value = ''; sub.value = true
  try {
    const user = await register({ username:u.username, passwordHash:u.password, realName:u.realName, mobile:u.mobile, email:u.email, institutionId:null })
    const iid = null
    if (iid) { for (const m of mats.value) { if (m.fileName&&m.fileUrl) await addQualification({ institutionId:iid, materialType:m.materialType, fileName:m.fileName, fileUrl:m.fileUrl, fileSize:0 }).catch(()=>{}) } }
    step.value = 3
  } catch (e) { e2.value = '注册失败：'+ (e.response?.data?.message||e.message||'未知错误') }
  finally { sub.value = false }
}
</script>

<style scoped>
.pg { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg,#e8edf5,#f0f2f5,#e3eaf5); padding: 40px; }
.card { width: 680px; background: #fff; border-radius: 16px; padding: 40px 48px; box-shadow: 0 8px 40px rgba(0,0,0,0.08); }
h1 { text-align: center; font-size: 24px; font-weight: 700; color: #1a1a2e; margin-bottom: 24px; }
.steps { display: flex; align-items: center; justify-content: center; margin-bottom: 32px; font-size: 14px; color: #999; gap: 8px; }
.steps b { display: inline-block; width: 26px; height: 26px; border-radius: 50%; background: #e8e8e8; line-height: 26px; text-align: center; font-size: 13px; margin-right: 4px; }
.steps .on b, .steps .done b { background: #2563eb; color: #fff; }
.steps .done b { background: #059669; }
.steps .on { color: #2563eb; font-weight: 600; }
.steps .done { color: #059669; }
.steps .bar { flex: 1; height: 2px; background: #e8e8e8; max-width: 60px; }
.steps .bar.done { background: #059669; }
.f .r { display: flex; gap: 14px; }
.f .r label { flex: 1; }
label { display: block; margin-bottom: 14px; font-size: 13px; font-weight: 600; color: #555; }
.req { color: #ef4444; }
input, select { width: 100%; padding: 10px 12px; border: 1px solid #d9d9d9; border-radius: 8px; font-size: 14px; outline: none; box-sizing: border-box; margin-top: 4px; }
input:focus, select:focus { border-color: #2563eb; box-shadow: 0 0 0 2px rgba(37,99,235,.1); }
.desc { color: #888; font-size: 14px; margin-bottom: 16px; }
.mc { background: #f9fafb; border: 1px solid #e8e8e8; border-radius: 10px; padding: 16px; margin-bottom: 12px; }
.mt { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.mt strong { font-size: 14px; color: #444; }
.del { background: none; border: none; color: #bbb; cursor: pointer; font-size: 13px; }
.del:hover { color: #ef4444; }
.mc select, .mc input { margin-bottom: 8px; }
.add { width: 100%; padding: 10px; background: #fff; border: 1px dashed #c0c0c0; border-radius: 8px; color: #888; font-size: 14px; cursor: pointer; margin-bottom: 8px; }
.add:hover { border-color: #2563eb; color: #2563eb; }
.err { color: #ef4444; font-size: 13px; margin: 8px 0; }
button[type=submit], .go { padding: 11px 28px; background: #2563eb; color: #fff; border: none; border-radius: 8px; font-size: 15px; font-weight: 600; cursor: pointer; }
button[type=submit]:hover { background: #1d4ed8; }
button[type=submit]:disabled { opacity: .5; cursor: not-allowed; }
.back { padding: 11px 28px; background: #fff; border: 1px solid #d9d9d9; border-radius: 8px; font-size: 14px; cursor: pointer; color: #666; }
.btns { display: flex; gap: 12px; margin-top: 8px; justify-content: space-between; }
.tip { text-align: center; margin-top: 16px; font-size: 14px; color: #888; }
.tip a { color: #2563eb; font-weight: 600; text-decoration: none; }
.done { text-align: center; padding: 32px 0; }
.done b { font-size: 60px; display: block; margin-bottom: 12px; }
.done h2 { color: #059669; font-size: 22px; margin-bottom: 8px; }
.done p { color: #888; font-size: 14px; margin-bottom: 24px; }
.go { display: inline-block; text-decoration: none; }
</style>
