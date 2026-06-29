<template>
  <div class="shell">
    <header class="topbar">
      <span class="logo">🏫 高校招生管理平台</span>
      <nav>
        <router-link to="/institution/workbench">工作台</router-link>
        <router-link to="/institution/enrollment-plan">招生计划</router-link>
        <router-link to="/institution/admission-guide">招生简章</router-link>
        <router-link to="/institution/admission-score">录取分数</router-link>
        <router-link to="/institution/settings">账号设置</router-link>
      </nav>
      <span class="user">👤 {{ name }} <button @click="quit">退出</button></span>
    </header>
    <main class="body">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { logout } from '@/api/institutionUser'
const router = useRouter()
const name = ref('')
onMounted(() => {
  // 从localStorage取登录态，没登录就踢回登录页
  const uid = localStorage.getItem('instUserId')
  const nameStr = localStorage.getItem('instUsername')
  if (!uid || uid === '' || uid === 'null' || uid === 'undefined') { router.push('/institution/login'); return }
  name.value = nameStr || ''
})
async function quit() {
  const uid = localStorage.getItem('instUserId')
  if (uid) await logout({ id: Number(uid) }).catch(() => {})
  localStorage.removeItem('instUserId')
  localStorage.removeItem('instUsername')
  localStorage.removeItem('instInstitutionId')
  router.push('/institution/login')
}
</script>

<style scoped>
.shell { display: flex; flex-direction: column; height: 100vh; background: #f0f2f5; }
.topbar {
  display: flex; align-items: center; gap: 48px; height: 56px; padding: 0 40px;
  background: #fff; border-bottom: 1px solid #e8e8e8; flex-shrink: 0;
}
.logo { font-size: 18px; font-weight: 700; color: #1a1a2e; }
nav { display: flex; gap: 4px; flex: 1; }
nav a {
  padding: 8px 22px; border-radius: 6px; text-decoration: none; font-size: 15px;
  color: #555; font-weight: 500;
}
nav a:hover { background: #f0f5ff; color: #2563eb; }
nav a.router-link-active { background: #2563eb; color: #fff; }
.user { display: flex; align-items: center; gap: 12px; font-size: 14px; color: #555; }
.user button {
  padding: 5px 16px; border: 1px solid #d9d9d9; border-radius: 6px;
  background: #fff; cursor: pointer; font-size: 13px; color: #666;
}
.user button:hover { color: #ef4444; border-color: #ef4444; }
.body { flex: 1; overflow-y: auto; padding: 32px 40px; }
</style>
