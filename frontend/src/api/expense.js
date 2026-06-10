import request from '@/utils/request'

// 我的报销列表（可选按状态过滤）
export function getExpenseList(status) {
  return request({ url: '/oa/expense/list', method: 'get', params: { status } })
}

// 报销详情
export function getExpenseById(id) {
  return request({ url: `/oa/expense/${id}`, method: 'get' })
}

// 提交报销申请
export function addExpense(data) {
  return request({ url: '/oa/expense', method: 'post', data })
}

// 修改报销申请
export function updateExpense(data) {
  return request({ url: '/oa/expense', method: 'put', data })
}

// 删除报销申请
export function deleteExpense(id) {
  return request({ url: `/oa/expense/${id}`, method: 'delete' })
}

// 报销统计
export function getExpenseStats() {
  return request({ url: '/oa/expense/stats', method: 'get' })
}
