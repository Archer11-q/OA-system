import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '',
  timeout: 15000
})

// 请求拦截器 — 注入 JWT Token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器 — 统一处理错误
request.interceptors.response.use(
  response => {
    const res = response.data
    // 文件下载等直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    if (res.code === 200) {
      return res
    }
    // Token 过期
    if (res.code === 401) {
      localStorage.removeItem('token')
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(new Error(res.message || '认证失败'))
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    // 尝试从响应体中提取后端返回的错误信息
    const serverMsg = error.response?.data?.message
    if (error.response?.status === 401 || error.response?.data?.code === 401) {
      localStorage.removeItem('token')
      router.push('/login')
      ElMessage.error(serverMsg || '登录已过期，请重新登录')
    } else {
      ElMessage.error(serverMsg || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
