<script setup>
import { computed, ref, watch, onMounted, onUnmounted, defineAsyncComponent } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const auth = useAuthStore()

// 学生端导航栏显示条件：非登录页、非院校端页面
const isStudentRoute = computed(() => !route.path.startsWith('/institution'))
const showNav = computed(() => {
  const name = route.name || ''
  return auth.isLoggedIn && !String(name).includes('Login') && isStudentRoute.value
})

const avatarKey = computed(() => 'my_avatar_' + (auth.userId || ''))
const avatar = ref(localStorage.getItem(avatarKey.value))

function syncAvatar() { avatar.value = localStorage.getItem(avatarKey.value) }
onMounted(() => window.addEventListener('avatar-updated', syncAvatar))
onUnmounted(() => window.removeEventListener('avatar-updated', syncAvatar))
watch(avatarKey, () => syncAvatar())

const navItems = [
  { to: '/score', icon: 'mdi:pencil-box-outline', label: '成绩信息' },
  { to: '/search', icon: 'mdi:magnify', label: '院校检索' },
  { to: '/ai', icon: 'mdi:robot-outline', label: 'AI 志愿' },
  { to: '/plan', icon: 'mdi:clipboard-text-outline', label: '志愿方案' },
  { to: '/favorites', icon: 'mdi:heart-outline', label: '我的收藏' },
  { to: '/center', icon: 'mdi:account-outline', label: '个人中心' },
]

// 后台管理判断（排除登录页，登录页不用侧边栏）
const isAdmin = computed(() => route.path.startsWith('/admin') && route.name !== 'adminLogin')

// 运营端布局：动态导入，CSS 只在访问 /admin 时才加载
const AdminLayout = defineAsyncComponent(() => import('@/layouts/AdminLayout.vue'))
</script>

<template>
  <!-- 后台管理 -->
  <AdminLayout v-if="isAdmin" />

  <!-- 学生端 -->
  <div v-else class="min-h-screen bg-gray-50">
    <header v-if="showNav" class="sticky top-0 z-50 bg-white border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-5 flex items-center justify-between h-16">
        <!-- Logo -->
        <RouterLink to="/ai" class="flex items-center gap-2.5 no-underline">
          <span class="w-9 h-9 bg-blue-600 rounded-full flex items-center justify-center">
            <Icon icon="mdi:school" class="text-white text-lg" />
          </span>
          <span class="text-lg font-bold text-gray-800">高考志愿分析系统</span>
        </RouterLink>

        <!-- Nav -->
        <nav class="flex items-center gap-1">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="flex items-center gap-1.5 px-3.5 py-2 text-sm text-gray-500 no-underline rounded-lg hover:bg-blue-50 hover:text-blue-600 transition-colors"
            active-class="!text-blue-600 !bg-blue-50 font-semibold"
          >
            <Icon :icon="item.icon" class="text-base" />
            <span>{{ item.label }}</span>
          </RouterLink>
<!-- Avatar -->
          <RouterLink to="/center" class="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center overflow-hidden hover:ring-2 ring-blue-300 transition-all">
            <img v-if="avatar" :src="avatar" class="w-full h-full object-cover" />
            <Icon v-else icon="mdi:account" class="text-white text-xl" />
          </RouterLink>
        </nav>
      </div>
    </header>

    <main :class="{ 'pt-0': !showNav }">
      <RouterView />
    </main>

  </div>
</template>
