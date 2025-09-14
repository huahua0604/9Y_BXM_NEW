import axios from 'axios'
import { useAuthStore } from '@/store/auth'

// 创建实例
const instance = axios.create({
  baseURL: 'http://localhost:8080', // 你的 Spring Boot 地址
  timeout: 10000
})

// 请求拦截器：带上 JWT token
instance.interceptors.request.use(config => {
  const auth = useAuthStore()
  if (auth?.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

// 响应拦截器：统一错误处理
instance.interceptors.response.use(
  res => res,
  err => {
    console.error('API Error:', err?.response || err)
    return Promise.reject(err)
  }
)

export default instance
