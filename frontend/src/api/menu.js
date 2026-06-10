import request from '@/utils/request'

// 菜单树（all=true返回全部，默认按用户权限）
export function getMenuTree(all) {
  return request({ url: '/oa/system/menu/tree', method: 'get', params: { all } })
}

// 新增菜单
export function addMenu(data) {
  return request({ url: '/oa/system/menu', method: 'post', data })
}

// 更新菜单
export function updateMenu(data) {
  return request({ url: '/oa/system/menu', method: 'put', data })
}

// 删除菜单
export function deleteMenu(id) {
  return request({ url: `/oa/system/menu/${id}`, method: 'delete' })
}
