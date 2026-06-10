import request from '@/utils/request'

// 角色列表
export function getRoleList() {
  return request({ url: '/oa/system/role/list', method: 'get' })
}

// 角色详情
export function getRoleById(id) {
  return request({ url: `/oa/system/role/${id}`, method: 'get' })
}

// 新增角色
export function addRole(data) {
  return request({ url: '/oa/system/role', method: 'post', data })
}

// 更新角色
export function updateRole(data) {
  return request({ url: '/oa/system/role', method: 'put', data })
}

// 删除角色
export function deleteRole(id) {
  return request({ url: `/oa/system/role/${id}`, method: 'delete' })
}

// 为角色分配菜单权限
export function assignRoleMenus(roleId, menuIds) {
  return request({ url: `/oa/system/role/${roleId}/menus`, method: 'put', data: { menuIds } })
}
