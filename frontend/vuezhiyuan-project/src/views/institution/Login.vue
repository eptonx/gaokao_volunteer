<template>
  <div class="pg">
    <div class="card">
      <div class="left">
        <h1>🏫</h1>
        <h2>高校招生管理平台</h2>
        <p>院校端 · 一站式招生信息管理</p>
        <ul>
          <li>📋 招生计划管理与批量导入</li>
          <li>📄 招生简章编辑与发布管理</li>
          <li>📊 历年录取分数线管理</li>
        </ul>
      </div>
      <div class="right">
        <h3>院校登录</h3>
        <form @submit.prevent="go">
          <label>用户名</label>
          <input v-model="f.username" name="username" placeholder="请输入用户名" required />
          <label>密码</label>
          <input v-model="f.password" name="password" type="password" placeholder="请输入密码" required />
          <p v-if="err" class="err">{{ err }}</p>
          <button type="submit" :disabled="ing">{{ ing ? '登录中...' : '登 录' }}</button>
        </form>
        <p class="tip">还没有账号？<router-link to="/institution/register">立即注册 →</router-link></p>
        <p class="tip" style="margin-top:4px"><router-link to="/login">← 考生端入口</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/institutionUser'
const router = useRouter()
const f = reactive({ username: '', password: '' })
const ing = ref(false), err = ref('')
// 登录成功后把用户信息和院校ID存localStorage，layout里会校验
async function go() {
  ing.value = true; err.value = ''
  try {
    const res = await login(f)
    localStorage.setItem('instUserId', res.id ?? '')
    localStorage.setItem('instUsername', res.username ?? '')
    localStorage.setItem('instInstitutionId', res.institutionId ?? '')
    router.push('/institution/workbench')
  } catch (e) {
    if (!e.response) err.value = '无法连接服务器，请确认后端已启动（端口8080）'
    else err.value = e.response.data?.message || '登录失败'
  }
  finally { ing.value = false }
}
</script>

<style scoped>
.pg {
  height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #e8edf5, #f0f2f5, #e3eaf5);
}
.card { display: flex; width: 820px; min-height: 460px; border-radius: 16px; overflow: hidden; box-shadow: 0 8px 40px rgba(0,0,0,0.08); }
.left {
  width: 380px; background: linear-gradient(160deg, #1a2a4a, #1e3a6e); color: #fff;
  padding: 48px 40px; display: flex; flex-direction: column; justify-content: center;
}
.left h1 { font-size: 52px; margin-bottom: 12px; }
.left h2 { font-size: 22px; font-weight: 700; margin-bottom: 8px; }
.left > p { font-size: 13px; color: #8fa4c4; margin-bottom: 32px; }
.left ul { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 10px; }
.left li { font-size: 14px; color: #c4d0e0; }
.right { flex: 1; background: #fff; padding: 48px 44px; display: flex; flex-direction: column; justify-content: center; }
.right h3 { font-size: 24px; font-weight: 700; color: #1a1a2e; margin-bottom: 28px; }
label { display: block; margin-bottom: 6px; font-size: 14px; font-weight: 600; color: #444; }
input {
  width: 100%; padding: 11px 14px; border: 1px solid #d9d9d9; border-radius: 8px;
  font-size: 15px; outline: none; box-sizing: border-box; margin-bottom: 18px;
}
input:focus { border-color: #2563eb; box-shadow: 0 0 0 2px rgba(37,99,235,0.1); }
button {
  width: 100%; padding: 12px; background: #2563eb; color: #fff; border: none;
  border-radius: 8px; font-size: 16px; font-weight: 600; cursor: pointer; margin-top: 8px;
}
button:hover { background: #1d4ed8; }
button:disabled { opacity: 0.55; cursor: not-allowed; }
.err { color: #ef4444; font-size: 13px; margin: 4px 0; }
.tip { text-align: center; margin-top: 22px; font-size: 14px; color: #888; }
.tip a { color: #2563eb; font-weight: 600; text-decoration: none; }
</style>
