import api from './index'

// 收藏（type: 1=院校 / 2=专业）
export function addFavorite(userId, targetId, type) {
  return api.post('/student/institution/favorite', null, { params: { userId, targetId, type } })
}

// 取消收藏
export function cancelFavorite(userId, targetId, type) {
  return api.delete('/student/institution/favorite', { params: { userId, targetId, type } })
}

// 我的收藏列表（type 可选，不传查全部）
export function getFavorites(userId, type) {
  const params = { userId }
  if (type) params.type = type
  return api.get('/student/institution/favorite/list', { params })
}

// 判断是否已收藏
export function checkFavorite(userId, targetId, type) {
  return api.get('/student/institution/check', { params: { userId, targetId, type } })
}
