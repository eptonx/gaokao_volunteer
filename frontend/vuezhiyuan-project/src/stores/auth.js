import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const userId = ref(Number(localStorage.getItem('userId')) || null)
  const accessToken = ref(localStorage.getItem('accessToken') || '')

  const isLoggedIn = computed(() => !!accessToken.value)

  async function login(credentials) {
    const res = await loginApi(credentials)
    userId.value = res.data.userId
    accessToken.value = res.data.accessToken
    localStorage.setItem('userId', res.data.userId)
    localStorage.setItem('accessToken', res.data.accessToken)
    return res
  }

  function logout() {
    userId.value = null
    accessToken.value = ''
    localStorage.removeItem('userId')
    localStorage.removeItem('accessToken')
  }

  return { userId, accessToken, isLoggedIn, login, logout }
})
