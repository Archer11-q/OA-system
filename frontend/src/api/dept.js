import request from '@/utils/request'

// 部门树
export function getDeptTree() {
  return request({ url: '/oa/system/dept/tree', method: 'get' })
}

// 新增部门
export function addDept(data) {
  return request({ url: '/oa/system/dept', method: 'post', data })
}

// 更新部门
export function updateDept(data) {
  return request({ url: '/oa/system/dept', method: 'put', data })
}

// 删除部门
export function deleteDept(id) {
  return request({ url: `/oa/system/dept/${id}`, method: 'delete' })
}

// 各部门用户统计（含子部门汇总）
export function getDeptUserStats() {
  return request({ url: '/oa/system/dept/user-stats', method: 'get' })
}
