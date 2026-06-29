import http from './index'

export const pageList = (params) => http.get('/admissionHistory/pageList', { params })
export const addScore = (data) => http.post('/admissionHistory/add', data)
export const updateScore = (data) => http.put('/admissionHistory/update', data)
export const deleteScore = (id) => http.delete('/admissionHistory/delete', { params: { id } })
