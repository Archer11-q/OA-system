import request from '@/utils/request'

// 公告列表（分页）
export function getNoticePage(params) {
  return request({ url: '/oa/notice/page', method: 'get', params })
}

// 公告详情
export function getNoticeById(id) {
  return request({ url: `/oa/notice/${id}`, method: 'get' })
}

// 发布公告
export function addNotice(data) {
  return request({ url: '/oa/notice', method: 'post', data })
}

// 编辑公告
export function updateNotice(data) {
  return request({ url: '/oa/notice', method: 'put', data })
}

// 删除公告
export function deleteNotice(id) {
  return request({ url: `/oa/notice/${id}`, method: 'delete' })
}
