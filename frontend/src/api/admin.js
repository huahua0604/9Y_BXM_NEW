import axios from '@/utils/axios'

export function adminCreateUser(payload) {
  return axios.post('/api/admin/users', payload).then(r => r.data)
}

export function adminListUsers() {
  return axios.get('/api/admin/users').then(r => r.data)
}

export function adminResetPassword(userId) {
  return axios.post(`/api/admin/users/${userId}/reset-password`).then(r => r.data)
}
