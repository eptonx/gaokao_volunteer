<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 左侧品牌区 -->
      <div class="card-left">
        <div class="brand-content">
          <div class="brand-icon">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="48" height="48" rx="12" fill="white" fill-opacity="0.2"/>
              <path d="M14 32V18L24 12L34 18V32L24 38L14 32Z" stroke="white" stroke-width="2" stroke-linejoin="round"/>
              <path d="M24 22V30" stroke="white" stroke-width="2" stroke-linecap="round"/>
              <circle cx="24" cy="18" r="1.5" fill="white"/>
            </svg>
          </div>
          <h1 class="brand-title">高考志愿分析系统</h1>
          <p class="brand-desc">运营管理平台</p>
          <div class="brand-features">
            <span>📊 数据仪表盘</span>
            <span>📝 简章审核</span>
            <span>🏫 院校管理</span>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="card-right">
        <div class="form-header">
          <h2>管理员登录</h2>
          <p>请输入账号密码登录运营端</p>
        </div>

        <el-form :model="loginForm" class="login-form" @keyup.enter="login">
          <el-form-item>
            <el-input
              v-model="loginForm.username"
              placeholder="请输入账号"
              size="large"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="login"
              class="login-btn"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <a href="/login" class="portal-link">考生端登录</a>
          <span class="divider">|</span>
          <a href="/institution/login" class="portal-link">院校端登录</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { loginApi } from '../assets/api/index'

const router = useRouter()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: '123456'
})

const login = async () => {
  loading.value = true
  try {
    await loginApi(loginForm)
    ElMessage.success('登录成功')
    router.push('/admin/dashboard')
  } catch {
    // 错误已在 request 拦截器中统一提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

/* ===== 背景装饰形状 ===== */
.bg-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
  animation: float 20s infinite ease-in-out;
}
.shape-1 {
  width: 400px; height: 400px;
  background: white;
  top: -100px; right: -100px;
}
.shape-2 {
  width: 300px; height: 300px;
  background: white;
  bottom: -80px; left: -60px;
  animation-delay: -7s;
}
.shape-3 {
  width: 200px; height: 200px;
  background: white;
  top: 50%; left: 45%;
  animation-delay: -14s;
}
@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

/* ===== 卡片容器 ===== */
.login-card {
  display: flex;
  width: 860px;
  min-height: 500px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* ===== 左侧品牌区 ===== */
.card-left {
  width: 380px;
  background: linear-gradient(135deg, #4f6ef6 0%, #7c3aed 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 50px 40px;
}
.brand-content { text-align: center; color: white; }
.brand-icon { display: inline-flex; margin-bottom: 24px; }
.brand-title { font-size: 22px; font-weight: 700; margin: 0 0 8px; letter-spacing: 1px; }
.brand-desc { font-size: 14px; opacity: 0.8; margin: 0 0 32px; }
.brand-features { display: flex; flex-direction: column; gap: 10px; }
.brand-features span { font-size: 13px; opacity: 0.75; letter-spacing: 0.5px; }

/* ===== 右侧表单区 ===== */
.card-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 50px 48px;
}
.form-header { margin-bottom: 36px; }
.form-header h2 { font-size: 26px; font-weight: 700; color: #1a1a2e; margin: 0 0 8px; }
.form-header p { font-size: 14px; color: #909399; margin: 0; }

.login-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 14px;
  box-shadow: 0 0 0 1px #e4e7ed inset;
  transition: box-shadow 0.3s;
}
.login-form :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px #c0c4cc inset; }
.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #4f6ef6 inset, 0 0 0 3px rgba(79, 110, 246, 0.15);
}
.login-form :deep(.el-input__inner) { font-size: 15px; }

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  letter-spacing: 4px;
  border-radius: 10px;
  background: linear-gradient(135deg, #4f6ef6 0%, #7c3aed 100%);
  border: none;
  transition: opacity 0.3s, transform 0.2s;
}
.login-btn:hover { opacity: 0.9; transform: translateY(-1px); }
.login-btn:active { transform: translateY(0); }

/* ===== 底部入口链接 ===== */
.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
.portal-link {
  color: #4f6ef6;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}
.portal-link:hover { color: #7c3aed; text-decoration: underline; }
.divider { color: #dcdfe6; }

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .login-card { width: 90vw; flex-direction: column; min-height: auto; }
  .card-left { width: 100%; padding: 30px 24px; }
  .brand-features { flex-direction: row; justify-content: center; gap: 16px; }
  .card-right { padding: 32px 28px; }
}
</style>
