import request from '@/utils/request'

export function getUserPage(params) {
  return request({ url: '/oa/system/user/page', method: 'get', params })
}

export function getUserById(id) {
  return request({ url: `/oa/system/user/${id}`, method: 'get' })
}

export function addUser(data) {
  return request({ url: '/oa/system/user', method: 'post', data })
}

export function updateUser(data) {
  return request({ url: '/oa/system/user', method: 'put', data })
}

export function deleteUser(id) {
  return request({ url: `/oa/system/user/${id}`, method: 'delete' })
}

export function resetPassword(id) {
  return request({ url: `/oa/system/user/${id}/reset-pwd`, method: 'put' })
}
