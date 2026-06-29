import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 6000,
  withCredentials: true
})

// 统一处理后端返回格式：兼容 Result<T> 包装 和 原始数据
service.interceptors.response.use(res => {
  const data = res.data
  // 如果后端用 Result<T> 包装了（有 code 字段），则进行状态判断和解包
  if (data !== null && typeof data === 'object' && 'code' in data) {
    if (data.code !== 200) {
      ElMessage.error(data.msg || '操作失败')
      return Promise.reject(data)
    }
    return data.data
  }
  // 否则直接返回原始数据（兼容未包装的接口）
  return data
}, err => {
  ElMessage.error('后端服务连接失败，请启动SpringBoot')
  return Promise.reject(err)
})

export default service