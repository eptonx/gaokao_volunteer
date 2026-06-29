<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { register as registerApi } from '@/api/auth'
import { Icon } from '@iconify/vue'

const router = useRouter()
const auth = useAuthStore()
const mode = ref('login')
const loginForm = reactive({ phone: '', password: '' })
const regForm = reactive({ phone: '', nickname: '', password: '' })
const logging = ref(false)
const registering = ref(false)
const loginError = ref('')
const regError = ref('')

async function handleLogin() {
  loginError.value = ''
  logging.value = true
  try {
    await auth.login(loginForm)
    router.push('/score')
  } catch (e) {
    loginError.value = e.response?.data?.msg || '登录失败'
  } finally {
    logging.value = false
  }
}

async function handleRegister() {
  regError.value = ''
  registering.value = true
  try {
    await registerApi(regForm)
    await auth.login({ phone: regForm.phone, password: regForm.password })
    router.push('/score')
  } catch (e) {
    regError.value = e.response?.data?.msg || '注册失败'
  } finally {
    registering.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 p-8">
    <div class="flex max-w-5xl w-full bg-white rounded-3xl overflow-hidden shadow-2xl">
      <!-- Left brand panel -->
      <div class="hidden lg:flex w-1/2 bg-gradient-to-br from-blue-600 to-indigo-900 p-12 text-white flex-col justify-between">
        <div>
          <div class="w-14 h-14 bg-white/20 rounded-2xl flex items-center justify-center mb-6">
            <Icon icon="mdi:school" class="text-3xl" />
          </div>
          <h2 class="text-3xl font-bold mb-3">高考志愿分析系统</h2>
          <p class="text-white/80 text-sm leading-relaxed mb-8">
            基于大数据与人工智能算法，为每一位考生提供精准、个性化的志愿填报建议，助您圆梦理想大学。
          </p>
          <div class="flex flex-col gap-3">
            <div class="flex items-center gap-3 text-sm text-white/90" v-for="f in ['3000+ 院校数据实时更新','AI 智能等位分换算','个性化志愿方案推荐']" :key="f">
              <span class="text-green-300 font-bold">✓</span> {{ f }}
            </div>
          </div>
        </div>
        <span class="text-xs text-white/50">© 2026 高考志愿分析系统</span>
      </div>

      <!-- Right form panel -->
      <div class="w-full lg:w-1/2 p-10 lg:p-14 flex flex-col justify-center">
        <h1 class="text-2xl font-bold text-gray-800 mb-1">欢迎回来</h1>
        <p class="text-sm text-gray-400 mb-6">开启您的智慧报考之旅</p>

        <!-- Tabs -->
        <div class="flex border-b border-gray-200 mb-6">
          <button :class="mode === 'login' ? 'text-blue-600 border-blue-600' : 'text-gray-400 border-transparent'"
            class="flex-1 pb-3 text-sm font-medium border-b-2 transition-colors" @click="mode = 'login'">手机登录</button>
          <button :class="mode === 'register' ? 'text-blue-600 border-blue-600' : 'text-gray-400 border-transparent'"
            class="flex-1 pb-3 text-sm font-medium border-b-2 transition-colors" @click="mode = 'register'">立即注册</button>
        </div>

        <!-- Login form -->
        <form v-if="mode === 'login'" @submit.prevent="handleLogin" class="flex flex-col gap-4">
          <input v-model="loginForm.phone" type="text" maxlength="11" placeholder="手机号" required
            class="px-4 py-3 border border-gray-200 rounded-lg text-sm outline-none focus:border-blue-500 bg-gray-50" />
          <input v-model="loginForm.password" type="password" placeholder="密码" required
            class="px-4 py-3 border border-gray-200 rounded-lg text-sm outline-none focus:border-blue-500 bg-gray-50" />
          <p v-if="loginError" class="text-red-500 text-xs">{{ loginError }}</p>
          <button type="submit" :disabled="logging"
            class="py-3 bg-blue-600 text-white rounded-lg font-medium text-sm hover:bg-blue-700 disabled:opacity-50 transition-colors shadow-lg shadow-blue-200">
            {{ logging ? '登录中...' : '登录' }}
          </button>
          <div class="text-xs text-gray-400 mt-1">
            <span class="cursor-pointer text-blue-500 hover:underline" @click="mode = 'register'">立即注册</span>
          </div>
          <div class="text-xs text-center text-gray-300 mt-4 pt-4 border-t border-gray-100 flex justify-center gap-4">
            <router-link to="/institution/login" class="text-gray-400 hover:text-blue-500 transition-colors">🏫 院校招生办入口</router-link>
            <router-link to="/admin/login" class="text-gray-400 hover:text-blue-500 transition-colors">⚙️ 运营端入口</router-link>
          </div>
        </form>

        <!-- Register form -->
        <form v-else @submit.prevent="handleRegister" class="flex flex-col gap-4">
          <input v-model="regForm.phone" type="text" maxlength="11" placeholder="手机号" required
            class="px-4 py-3 border border-gray-200 rounded-lg text-sm outline-none focus:border-blue-500 bg-gray-50" />
          <input v-model="regForm.nickname" type="text" placeholder="昵称" required
            class="px-4 py-3 border border-gray-200 rounded-lg text-sm outline-none focus:border-blue-500 bg-gray-50" />
          <input v-model="regForm.password" type="password" placeholder="密码（至少6位）" required
            class="px-4 py-3 border border-gray-200 rounded-lg text-sm outline-none focus:border-blue-500 bg-gray-50" />
          <p v-if="regError" class="text-red-500 text-xs">{{ regError }}</p>
          <button type="submit" :disabled="registering"
            class="py-3 bg-blue-600 text-white rounded-lg font-medium text-sm hover:bg-blue-700 disabled:opacity-50 transition-colors shadow-lg shadow-blue-200">
            {{ registering ? '注册中...' : '注册' }}
          </button>
          <p class="text-xs text-center text-gray-400">
            已有账号？<span class="text-blue-500 cursor-pointer hover:underline" @click="mode = 'login'">去登录</span>
          </p>
        </form>

      </div>
    </div>
  </div>
</template>
