import request from '@/utils/request'

// 签到
export function signIn(data) {
  return request({ url: '/oa/attendance/sign-in', method: 'post', data })
}

// 签退
export function signOut(data) {
  return request({ url: '/oa/attendance/sign-out', method: 'post', data })
}

// 考勤记录（按月查询，格式 yyyy-MM）
export function getAttendanceRecords(month) {
  return request({ url: '/oa/attendance/records', method: 'get', params: { month } })
}

// 请假申请
export function submitLeave(data) {
  return request({ url: '/oa/attendance/leave', method: 'post', data })
}

// 月度考勤汇总统计
export function getMonthlyReport(month) {
  return request({ url: '/oa/attendance/monthly-report', method: 'get', params: { month } })
}

// 月度每日考勤状态明细
export function getDailyStatus(month) {
  return request({ url: '/oa/attendance/daily-status', method: 'get', params: { month } })
}
