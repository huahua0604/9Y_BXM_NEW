import axios from 'axios';
axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

axios.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export async function fetchMyApprovals(params) {
  const res = await axios.get('/api/review/my-approvals', { params });
  return res.data;
}
