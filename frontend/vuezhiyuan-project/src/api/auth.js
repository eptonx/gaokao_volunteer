import api from './index'

export function register(data) {
  return api.post('/auth/register', data)
}

export function login(data) {
  return api.post('/auth/login', data)
}

// 登录记录（注意：后端 /login-record/list 不走 Result 包装，直接返回数组）
export function getLoginRecords(userId) {
  return api.get(`/login-record/list/${userId}`)
}
