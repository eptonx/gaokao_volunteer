import http from './index'

// 提交资质材料
export const addQualification = (data) => http.post('/qualification/add', data)

// 查询某院校的资质审核状态
export const getQualifications = (institutionId) =>
  http.get('/qualification/byInstitution', { params: { institutionId } })
