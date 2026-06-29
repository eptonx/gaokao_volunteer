import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const http = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

// 请求拦截器：自动注入 Bearer token
http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

// 响应拦截器：统一解包 data + 业务码检查 + 401 自动跳登录
http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body === 'object' && 'code' in body && body.code !== 200) {
      return Promise.reject({ response: { data: body, status: res.status } })
    }
    return body
  },
  (err) => {
    if (err.response?.status === 401) {
      const auth = useAuthStore()
      auth.logout()
      window.location.href = '/login'
    }
    console.error('请求失败:', err)
    return Promise.reject(err)
  },
)

export default http

// ==================== 院校库 ====================
export const institutionApi = {
  list: () => http.get('/institution/list'),
  getById: (id) => http.get(`/institution/${id}`),
  add: (data) => http.post('/institution/add', data),
  update: (data) => http.put('/institution/update', data),
  delete: (id) => http.delete(`/institution/delete/${id}`),
  onlineStatus: (id, isOnline) =>
    http.put('/institution/onlineStatus', null, { params: { id, isOnline } }),
  updateLevel: (id, level) =>
    http.put('/institution/level', null, { params: { id, level } }),
  updateStatus: (id, status) =>
    http.put('/institution/status', null, { params: { id, status } }),
}

// ==================== 专业库 ====================
export const majorDictApi = {
  list: (majorName) => http.get('/majorDict/list', { params: { majorName } }),
  getById: (id) => http.get('/majorDict/getById', { params: { id } }),
  save: (data) => http.post('/majorDict/save', data),
  update: (data) => http.put('/majorDict/update', data),
  delete: (id) => http.delete('/majorDict/delete', { params: { id } }),
  onlineStatus: (id, status) =>
    http.put('/majorDict/onlineStatus', null, { params: { id, status } }),
}

// ==================== 历年录取数据 ====================
export const admissionHistoryApi = {
  selectAll: () => http.get('/admissionHistory/selectAll'),
  selectById: (id) => http.get('/admissionHistory/selectById', { params: { id } }),
  add: (data) => http.post('/admissionHistory/add', data),
  update: (data) => http.put('/admissionHistory/update', data),
  delete: (id) => http.delete('/admissionHistory/delete', { params: { id } }),
  publish: (id, status) =>
    http.put('/admissionHistory/publish', null, { params: { id, status } }),
  import: (file) => {
    const form = new FormData()
    form.append('file', file)
    return http.post('/admissionHistory/import', form)
  },
  downloadTemplate: () =>
    http.get('/admissionHistory/template', { responseType: 'blob' }),
}

// ==================== 导入日志 ====================
export const dataImportLogApi = {
  selectAll: () => http.get('/dataImportLog/selectAll'),
  selectById: (id) => http.get('/dataImportLog/selectById', { params: { id } }),
}
