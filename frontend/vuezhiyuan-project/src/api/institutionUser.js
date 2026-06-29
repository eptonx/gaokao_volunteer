import http from './index'

// 注册
export const register = (data) => http.post('/institutionUser/register', data)

// 登录
export const login = (data) => http.post('/institutionUser/login', data)

// 改密码
export const changePassword = (data) => http.put('/institutionUser/changePassword', data)

// 登出
export const logout = (data) => http.post('/institutionUser/logout', data)

// 查询单个用户信息
export const getUserInfo = (id) => http.get('/institutionUser/getOne', { params: { id } })

// 更新用户信息
export const updateUser = (data) => http.put('/institutionUser/update', data)

// 注销账户
export const deleteAccount = (id) => http.delete('/institutionUser/delete', { params: { id } })
