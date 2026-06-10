import request from '@/utils/request'

export function getOverview() {
  return request({ url: '/oa/dashboard/overview', method: 'get' })
}

export function getAttendanceTrend() {
  return request({ url: '/oa/dashboard/attendance-trend', method: 'get' })
}

export function getApprovalDistribution() {
  return request({ url: '/oa/dashboard/approval-distribution', method: 'get' })
}

export function getExpenseDistribution() {
  return request({ url: '/oa/dashboard/expense-distribution', method: 'get' })
}
