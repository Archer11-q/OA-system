import request from '@/utils/request'

// ===== 审批实例 =====

// 发起审批
export function startApproval(data) {
  return request({ url: '/oa/approval/start', method: 'post', data })
}

// 待审批列表
export function getApprovalTodo() {
  return request({ url: '/oa/approval/todo', method: 'get' })
}

// 已审批列表
export function getApprovalDone() {
  return request({ url: '/oa/approval/done', method: 'get' })
}

// 我的申请
export function getApprovalMy() {
  return request({ url: '/oa/approval/my', method: 'get' })
}

// 审批操作（同意/驳回）
export function approveInstance(id, data) {
  return request({ url: `/oa/approval/${id}/approve`, method: 'post', data })
}

// 撤回审批
export function cancelInstance(id) {
  return request({ url: `/oa/approval/${id}/cancel`, method: 'post' })
}

// 审批记录
export function getApprovalRecords(id) {
  return request({ url: `/oa/approval/${id}/records`, method: 'get' })
}

// ===== 审批模板 =====

// 模板列表
export function getTemplateList() {
  return request({ url: '/oa/approval/template/list', method: 'get' })
}

// 模板详情
export function getTemplateById(id) {
  return request({ url: `/oa/approval/template/${id}`, method: 'get' })
}

// 新增模板
export function addTemplate(data) {
  return request({ url: '/oa/approval/template', method: 'post', data })
}

// 更新模板
export function updateTemplate(data) {
  return request({ url: '/oa/approval/template', method: 'put', data })
}

// 删除模板
export function deleteTemplate(id) {
  return request({ url: `/oa/approval/template/${id}`, method: 'delete' })
}
