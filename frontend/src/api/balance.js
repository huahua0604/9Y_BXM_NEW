import axios from '@/utils/axios'

export function getBalance() {
  return axios.get('/api/admin/balance').then(r => r.data)
}

export function addFunds(payload) {
  // payload: { amount: number|string, note?: string }
  return axios.post('/api/admin/balance/add', payload).then(r => r.data)
}

export function listTx() {
  return axios.get('/api/admin/balance/tx').then(r => r.data)
}
