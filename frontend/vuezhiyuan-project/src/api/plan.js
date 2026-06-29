import api from './index'

// ==================== 方案 CRUD ====================

/** 查询用户的所有志愿方案 */
export function listPlans(userId) {
  return api.get('/student/plan/list', { params: { userId } })
}

/** 查询单个方案 */
export function getPlan(id) {
  return api.get('/student/plan/get', { params: { id } })
}

/** 查询用户已锁定的方案 */
export function getLockedPlan(userId) {
  return api.get('/student/plan/getLocked', { params: { userId } })
}

/** 手动创建空方案（不生成详情） */
export function addPlan(data) {
  return api.post('/student/plan/add', data)
}

/** 修改方案信息 */
export function updatePlan(data) {
  return api.put('/student/plan/update', data)
}

/** 删除方案（含详情） */
export function deletePlan(id) {
  return api.delete('/student/plan/delete', { params: { id } })
}

// ==================== AI 生成 ====================

/** AI 智能生成志愿方案（含 冲稳保 梯度详情 + 风险分析，可传入 sessionId 关联对话上下文） */
export function generatePlan(planName, sessionId) {
  return api.post('/student/plan/generate', null, { params: { planName, sessionId } })
}

// ==================== 方案详情（含院校/专业名称） ====================

/**
 * 查询方案志愿详情 —— 返回带 institutionName / majorName 的 VO
 * 替代旧的 /student/planDetail/listByPlan
 */
export function listPlanDetails(planId) {
  return api.get('/student/plan/detail/list', { params: { planId } })
}

// ==================== 风险分析（动态生成，不持久化） ====================

/** 获取方案的 AI 风险分析（每次调用实时生成） */
export function getRiskAnalysis(planId) {
  return api.get('/student/plan/riskAnalysis', { params: { planId } })
}

// ==================== 锁定 / 解锁 ====================

/** 锁定方案 */
export function lockPlan(planId) {
  return api.post('/student/plan/lock', null, { params: { planId } })
}

/** 解锁方案 */
export function unlockPlan(planId) {
  return api.post('/student/plan/unlock', null, { params: { planId } })
}

// ==================== 导出 ====================

/** 导出志愿方案为 HTML 报告（含院校/专业/风险分析） */
export function exportReport(planId) {
  return api.get('/student/plan/export', { params: { planId } })
}

// ==================== Detail 旧接口兼容 ====================

/** @deprecated 使用 listPlanDetails 替代（/student/plan/detail/list） */
export function listPlanDetailsRaw(planId) {
  return api.get('/student/planDetail/listByPlan', { params: { planId } })
}

/** 手动添加单条志愿详情 */
export function addPlanDetail(data) {
  return api.post('/student/planDetail/add', data)
}

/** 批量添加志愿详情 */
export function batchAddPlanDetails(details) {
  return api.post('/student/planDetail/batchAdd', details)
}

/** 修改单条志愿详情 */
export function updatePlanDetail(data) {
  return api.put('/student/planDetail/update', data)
}

/** 清空方案所有详情 */
export function deletePlanDetails(planId) {
  return api.delete('/student/planDetail/deleteByPlan', { params: { planId } })
}
