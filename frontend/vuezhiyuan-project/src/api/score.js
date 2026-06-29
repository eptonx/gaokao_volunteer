import api from './index'

export function getScores(userId) {
  return api.get('/student/score/list', { params: { userId } })
}

export function getScoreById(id) {
  return api.get('/student/score/get', { params: { id } })
}

export function addScore(data) {
  return api.post('/student/score/add', data)
}

export function updateScore(data) {
  return api.put('/student/score/update', data)
}

export function deleteScore(id) {
  return api.delete('/student/score/delete', { params: { id } })
}

export function calcEquivalent(params) {
  return api.get('/student/score/equivalent', { params })
}

export function rankToScore(params) {
  return api.get('/student/score/rank-to-score', { params })
}
