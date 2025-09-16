import axios from '@/utils/axios'

export function createRequest(payload) {
  return axios.post('/api/requests', payload).then(r => r.data) 
}

export function uploadAttachment(requestId, file) {
  const fd = new FormData()
  fd.append('file', file)
  return axios.post(`/api/requests/${requestId}/attachments`, fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(r => r.data)
}

export function listAttachments(requestId) {
  return axios.get(`/api/requests/${requestId}/attachments`).then(r => r.data)
}

export function submitRequest(requestId) {
  return axios.post(`/api/requests/${requestId}/submit`).then(r => r.data)
}
