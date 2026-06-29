import api from './index'

// 查询个人信息
export function getUserInfo(userId) {
  return api.get('/student/center/info', { params: { userId } })
}

// 更新个人信息
export function updateUserInfo(user) {
  return api.put('/student/center/update', user)
}

// 查询考生成绩
export function getStudentScores(userId) {
  return api.get('/student/score/list', { params: { userId } })
}

// 保存成绩
export function saveStudentScore(score) {
  return api.post('/student/score/add', score)
}

// 方案锁定记录
export function getLockRecords(userId) {
  return api.get('/student/center/lockRecords', { params: { userId } })
}

// 查询志愿方案列表
export function getVolunteerPlans(userId) {
  return api.get('/student/plan/list', { params: { userId } })
}

// 修改密码
export function changePassword(data) {
  return api.post('/student/center/changePassword', data)
}
