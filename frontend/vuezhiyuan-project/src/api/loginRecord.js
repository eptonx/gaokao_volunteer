import request from '@/utils/request'

// 登录日志分页
export const getLoginRecordPage = (params) => request.get('/login-record/page', { params })
