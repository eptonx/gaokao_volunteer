import http from './index'

// 招生简章分页列表
export const pageList = (params) => http.get('/admission-guide/pageList', { params })

// 单条新增
export const addGuide = (data) => http.post('/admission-guide', data)

// 修改
export const updateGuide = (data) => http.put('/admission-guide', data)

// 删除
export const deleteGuide = (id) => http.delete(`/admission-guide/${id}`)

// 发布/撤回
export const publishGuide = (data) =>
  http.put('/admission-guide/publish', data)

// 待审核列表
export const getReviewList = () => http.get('/admission-guide/review-list')

// 查详情
export const getDetail = (id) => http.get(`/admission-guide/${id}`)

// 已审核列表
export const getReviewedList = () => http.get('/admission-guide/reviewed-list')

// 审核通过/驳回
export const review = (id, reviewStatus, reviewComment, reviewerId) =>
  http.put(`/admission-guide/${id}/review`, { reviewStatus, reviewComment, reviewerId })

// AI提取结果
export const getAiResult = (id) => http.get(`/admission-guide/${id}/ai-result`)
