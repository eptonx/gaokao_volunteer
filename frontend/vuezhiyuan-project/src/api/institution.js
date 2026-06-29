import http from './index'

// ========== 考生端：院校查询 ==========

// 多条件筛选院校列表
export function filterInstitutions(params, pageNum = 1, pageSize = 10) {
  return http.get('/student/institution/list', { params: { ...params, pageNum, pageSize } })
}

// 关键词搜索院校
export function searchInstitutions(keyword, pageNum = 1, pageSize = 10) {
  return http.get('/student/institution/search', { params: { keyword, pageNum, pageSize } })
}

// 筛选项字典
export function getFilterOptions() {
  return http.get('/student/institution/filterOptions')
}

// 院校详情
export function getInstitutionDetail(id) {
  return http.get(`/student/institution/${id}`)
}

// 院校招生专业列表
export function getInstitutionMajors(id) {
  return http.get(`/student/institution/${id}/majors`)
}

// 院校历年录取分
export function getInstitutionScores(id) {
  return http.get(`/student/institution/${id}/admissionScores`)
}

// 院校已发布招生简章
export function getInstitutionGuides(id) {
  return http.get(`/student/institution/${id}/admissionGuides`)
}

// ========== 院校端：自维护 ==========

// 查院校信息
export const getInstitution = (id) => http.get(`/institution/${id}`)

// 院校自维护字段更新（logo、简介、官网、联系方式、地址）
export const updateSelf = (data) => http.put('/institution/updateSelf', data)
