import axios from '@/utils/axios'

// 新建报销单（草稿）
export function createRequest(payload) {
  // payload: { title, amount, category, occurDate, remark }
  return axios.post('/api/requests', payload).then(r => r.data) // 返回新建后的对象（含 id）
}

// 上传附件（PDF/图片）
export function uploadAttachment(requestId, file) {
  const fd = new FormData()
  fd.append('file', file)
  return axios.post(`/api/requests/${requestId}/attachments`, fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(r => r.data) // 返回附件ID或对象
}

// 查询附件列表
export function listAttachments(requestId) {
  return axios.get(`/api/requests/${requestId}/attachments`).then(r => r.data)
}

// 提交审核
export function submitRequest(requestId) {
  return axios.post(`/api/requests/${requestId}/submit`).then(r => r.data)
}
