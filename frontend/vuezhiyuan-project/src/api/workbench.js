import http from './index'

// 工作台首页数据概览
export const overview = (institutionId) =>
  http.get('/workbench/overview', { params: { institutionId } })
