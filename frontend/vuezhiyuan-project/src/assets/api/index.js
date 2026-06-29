import request from '../../utils/request'
// ====================== 管理员登录模块 ======================
/**
 * 管理员登录
 * @param {Object} form {username, password}
 */
export const loginApi = (form) => {
  return request({
    url: '/admin/login',
    method: 'post',
    data: form
  })
}

/**
 * 退出登录
 */
export const logoutApi = () => {
  return request({
    url: '/admin/logout',
    method: 'post'
  })
}

/**
 * 修改密码
 * @param {Object} form {oldPwd, newPwd}
 */
export const updatePwdApi = (form) => {
  return request({
    url: '/admin/updatePwd',
    method: 'post',
    data: form
  })
}
// ====================== 管理员管理模块 ======================
/**
 * 管理员分页列表（支持关键词搜索）
 * @param {Number} pageNum 页码
 * @param {Number} pageSize 每页条数
 * @param {String} keyword 搜索关键词（可选）
 */
export const getAdminPageApi = (pageNum, pageSize, keyword) => {
  return request({
    url: '/admin/selectAll',
    method: 'get',
    params: { pageNum, pageSize, keyword }
  })
}

/**
 * 新增管理员
 * @param {Object} form {username, passwordHash, realName, roleId, status}
 */
export const addAdminApi = (form) => {
  return request({
    url: '/admin/add',
    method: 'post',
    data: form
  })
}

/**
 * 修改管理员信息
 * @param {Object} form {id, username, realName, roleId, status}
 */
export const updateAdminApi = (form) => {
  return request({
    url: '/admin/update',
    method: 'put',
    data: form
  })
}

/**
 * 删除管理员
 * @param {Number} id 管理员id
 */
export const deleteAdminApi = (id) => {
  return request({
    url: '/admin/delete',
    method: 'delete',
    params: { id }
  })
}

/**
 * 启用/禁用管理员
 * @param {Number} id 管理员id
 * @param {Number} status 0禁用 1启用
 */
export const changeAdminStatusApi = (id, status) => {
  return request({
    url: '/admin/updateStatus',
    method: 'put',
    params: { id, status }
  })
}

/**
 * 给管理员分配角色
 * @param {Number} id 管理员id
 * @param {Number} roleId 角色id
 */
export const assignRoleApi = (id, roleId) => {
  return request({
    url: '/admin/updateRoleId',
    method: 'put',
    params: { id, roleId }
  })
}

/**
 * 获取全部角色列表（只读）
 */
export const getRoleListApi = () => {
  return request({
    url: '/role/list',
    method: 'get'
  })
}

/**
 * 启用/禁用角色
 * @param {Number} id 角色id
 * @param {Number} status 0禁用 1启用
 */
export const updateRoleStatusApi = (id, status) => {
  return request({
    url: '/role/updateStatus',
    method: 'put',
    params: { id, status }
  })
}
// ====================== 资质审核模块 ======================
/**
 * 待审核资质分页列表
 * @param {Number} pageNum
 * @param {Number} pageSize
 * @param {String} keyword 文件名搜索（可选）
 * @param {Number} institutionId 院校ID筛选（可选）
 */
export const getWaitAuditApi = (pageNum, pageSize, keyword, institutionId) => {
  return request({
    url: '/qualification/waitList',
    method: 'get',
    params: { pageNum, pageSize, keyword, institutionId }
  })
}

/**
 * 查看资质详情
 * @param {Number} id 资质id
 */
export const getQualDetailApi = (id) => {
  return request({
    url: '/qualification/detail',
    method: 'get',
    params: { id }
  })
}

/**
 * 审核通过
 * @param {Number} id
 * @param {String} comment 审核意见
 */
export const auditPassApi = (id, comment) => {
  return request({
    url: '/qualification/pass',
    method: 'post',
    params: { id, comment }
  })
}

/**
 * 审核驳回
 * @param {Number} id
 * @param {String} comment 驳回意见（必填）
 */
export const auditRejectApi = (id, comment) => {
  return request({
    url: '/qualification/reject',
    method: 'post',
    params: { id, comment }
  })
}

/**
 * 审核历史列表（已通过/驳回，支持搜索）
 * @param {String} keyword 文件名搜索（可选）
 */
export const getAuditHistoryApi = (keyword) => {
  return request({
    url: '/qualification/history',
    method: 'get',
    params: { keyword }
  })
}