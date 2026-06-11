import request from '@/utils/request'

// 日程列表（按日期范围）
export function getScheduleList(params) {
  return request({ url: '/oa/schedule/list', method: 'get', params })
}

// 日程详情
export function getScheduleById(id) {
  return request({ url: `/oa/schedule/${id}`, method: 'get' })
}

// 新增日程
export function addSchedule(data) {
  return request({ url: '/oa/schedule', method: 'post', data })
}

// 更新日程
export function updateSchedule(data) {
  return request({ url: '/oa/schedule', method: 'put', data })
}

// 删除日程
export function deleteSchedule(id) {
  return request({ url: `/oa/schedule/${id}`, method: 'delete' })
}

// 获取即将到来的日程提醒
export function getScheduleReminders() {
  return request({ url: '/oa/schedule/reminders', method: 'get' })
}
