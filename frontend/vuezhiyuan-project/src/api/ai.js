import api from './index'

// ==================== 会话管理 ====================

/** 获取用户的会话列表（去重，按最近时间排序） */
export function listSessions() {
  return api.get('/student/ai/sessions')
}

/** 删除整个会话 */
export function deleteSession(sessionId) {
  return api.delete('/student/ai/deleteSession', { params: { sessionId } })
}

// ==================== 消息查询 ====================

/** 查询某个会话的全部历史消息 */
export function getHistory(sessionId) {
  return api.get('/student/ai/history', { params: { sessionId } })
}

/** @deprecated 使用 getHistory 替代 */
export function listBySession(sessionId) {
  return api.get('/student/ai/history', { params: { sessionId } })
}

/** 查询用户的所有消息（原始数据，一般用 sessions 替代） */
export function listByUser(userId) {
  return api.get('/student/ai/listByUser', { params: { userId } })
}

// ==================== AI 对话 ====================

/** 非流式对话 —— 发送消息，获取完整回复 */
export function chat({ message, sessionId }) {
  return api.post('/student/ai/chat', null, { params: { message, sessionId } })
}

/**
 * 流式对话 —— 返回 fetch Response 对象供 SSE 读取
 * 前端用法：
 *   const resp = await chatStream({ message, sessionId })
 *   const reader = resp.body.getReader()
 *   // 逐行解析 SSE ...
 */
export function chatStream({ message, sessionId }, token) {
  const params = new URLSearchParams({ message, sessionId })
  return fetch(`/api/student/ai/chat/stream?${params}`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
      Accept: 'text/event-stream',
    },
  })
}

// ==================== 辅助 ====================

/** 手动添加对话记录（管理后台使用） */
export function addConversation(data) {
  return api.post('/student/ai/add', data)
}
