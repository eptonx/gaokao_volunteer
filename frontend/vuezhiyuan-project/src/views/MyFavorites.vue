<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { getFavorites, cancelFavorite } from '@/api/favorite.js'
import { getInstitutionDetail } from '@/api/institution.js'
import { useAuthStore } from '@/stores/auth'

const router = useRouter(); const auth = useAuthStore()
const loading = ref(false); const items = ref([])

onMounted(() => loadAll())

async function loadAll() {
  loading.value = true
  try {
    const fr = await getFavorites(auth.userId, 1); const favs = fr?.data || fr || []
    const results = await Promise.allSettled(favs.map(async f => {
      const ir = await getInstitutionDetail(f.targetId); const inst = ir?.data || ir
      return { fav: f, name: inst.institutionName, detail: inst }
    }))
    items.value = results.map((r, i) => r.status === 'fulfilled' ? r.value : { fav: favs[i], name: '未知院校', detail: {} })
  } catch { items.value = [] } finally { loading.value = false }
}

async function removeItem(item) {
  try { await cancelFavorite(auth.userId, item.fav.targetId, item.fav.type); items.value = items.value.filter(i => i !== item) } catch {}
}
function goDetail(item) { router.push(`/institution/${item.fav.targetId}`) }
</script>

<template>
  <div class="h-[calc(100vh-4rem)] flex flex-col max-w-7xl mx-auto p-4 lg:p-6">
    <div class="flex items-center gap-3 mb-6">
      <h2 class="text-2xl font-bold text-gray-800">我的收藏</h2>
      <span class="text-xs text-gray-400">管理关注的院校</span>
      <span v-if="items.length" class="ml-auto px-3 py-1 bg-gray-100 rounded-full text-xs text-gray-500">{{ items.length }} 项</span>
    </div>

    <div v-if="loading" class="text-center py-20 text-gray-400">加载中...</div>

    <div v-else-if="items.length" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
      <div v-for="item in items" :key="item.fav.id"
        class="flex items-center gap-4 p-5 bg-white rounded-xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow group">
          <img :src="item.detail.logoUrl || '/favicon.ico'" class="w-12 h-12 rounded-xl object-cover bg-gray-100 flex-shrink-0" @error="e => e.target.style.display='none'" />
        <div class="flex-1 min-w-0 cursor-pointer" @click="goDetail(item)">
          <h4 class="font-semibold text-gray-800 text-sm truncate">{{ item.name }}</h4>
          <div class="flex flex-wrap gap-3 mt-1.5 text-xs text-gray-400">
            <span v-if="item.detail.level">{{ item.detail.level }}</span>
            <span v-if="item.detail.address">📍 {{ item.detail.address }}</span>
          </div>
        </div>
        <button @click="removeItem(item)"
          class="p-2 text-red-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors flex-shrink-0">
          <Icon icon="mdi:heart" class="text-lg" />
        </button>
      </div>
    </div>

    <div v-else class="text-center py-20 text-gray-400">
      <p class="text-5xl mb-3">💔</p>
      <p class="text-sm mb-4">暂无收藏</p>
      <button @click="router.push('/search')" class="px-6 py-2.5 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors">去浏览院校</button>
    </div>
  </div>
</template>
