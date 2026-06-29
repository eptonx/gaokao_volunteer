import http from './index'

// 分页列表查询
export const pageList = (params) => http.get('/plan/pageList', { params })

// 单条新增
export const addPlan = (data) => http.post('/plan/add', data)

// 批量导入（JSON数组）
export const batchAdd = (data) => http.post('/plan/batchAdd', data)

// 修改
export const updatePlan = (data) => http.put('/plan/update', data)

// 删除
export const deletePlan = (id) => http.delete('/plan/delete', { params: { id } })

// 发布/撤回
export const publishPlan = (data) => http.put('/plan/publish', data)
