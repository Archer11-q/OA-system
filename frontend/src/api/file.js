import axios from 'axios'

/**
 * 文件上传 API
 * 使用独立 axios 实例（multipart/form-data），不同于 JSON 请求
 */

const uploadRequest = axios.create({
  baseURL: '',
  timeout: 30000
})

// 请求拦截器 — 注入 JWT Token
uploadRequest.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
uploadRequest.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    return Promise.reject(new Error(res.message || '上传失败'))
  },
  error => {
    const serverMsg = error.response?.data?.message
    return Promise.reject(new Error(serverMsg || error.message || '上传失败'))
  }
)

/**
 * 通用文件上传
 * @param {File} file - 上传文件
 * @returns {Promise<{fileName: string, originalName: string, url: string, size: string}>}
 */
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return uploadRequest({
    url: '/oa/file/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 头像上传（仅限图片格式）
 * @param {File} file - 头像图片文件
 * @returns {Promise<{fileName: string, url: string}>}
 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return uploadRequest({
    url: '/oa/file/upload/avatar',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
