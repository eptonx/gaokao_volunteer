import http from './index'

// 上传附件信息（实际文件上传需用 FormData + multipart）
export const addAttachment = (data) => http.post('/attachment/add', data)

// 上传图片文件，返回 { success, fileName, fileUrl, fileSize }
export const uploadFile = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return http.post('/file/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}
