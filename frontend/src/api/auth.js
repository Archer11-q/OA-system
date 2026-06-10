import request from '@/utils/request'

export function loginApi(data) {
  return request({ url: '/oa/auth/login', method: 'post', data })
}

export function getUserInfoApi() {
  return request({ url: '/oa/auth/user-info', method: 'get' })
}

export function logoutApi() {
  return request({ url: '/oa/auth/logout', method: 'post' })
}
