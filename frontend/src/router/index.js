import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 布局
const Layout = () => import('@/layout/index.vue')

// 动态导入视图组件
const Login = () => import('@/views/login/index.vue')
const Dashboard = () => import('@/views/dashboard/index.vue')
const UserList = () => import('@/views/system/user/index.vue')
const RoleList = () => import('@/views/system/role/index.vue')
const MenuList = () => import('@/views/system/menu/index.vue')
const DeptList = () => import('@/views/system/dept/index.vue')
const Attendance = () => import('@/views/attendance/index.vue')
const Approval = () => import('@/views/approval/index.vue')
const Notice = () => import('@/views/notice/index.vue')
const Schedule = () => import('@/views/schedule/index.vue')
const Expense = () => import('@/views/expense/index.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '数据看板', icon: 'DataAnalysis' }
      },
      {
        path: 'system',
        name: 'System',
        meta: { title: '系统管理', icon: 'Setting' },
        children: [
          {
            path: 'user',
            name: 'UserList',
            component: UserList,
            meta: { title: '用户管理' }
          },
          {
            path: 'role',
            name: 'RoleList',
            component: RoleList,
            meta: { title: '角色管理' }
          },
          {
            path: 'menu',
            name: 'MenuList',
            component: MenuList,
            meta: { title: '菜单管理' }
          },
          {
            path: 'dept',
            name: 'DeptList',
            component: DeptList,
            meta: { title: '部门管理' }
          }
        ]
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: Attendance,
        meta: { title: '考勤管理', icon: 'Timer' }
      },
      {
        path: 'approval',
        name: 'Approval',
        component: Approval,
        meta: { title: '审批中心', icon: 'DocumentChecked' }
      },
      {
        path: 'notice',
        name: 'Notice',
        component: Notice,
        meta: { title: '公告通知', icon: 'Bell' }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: Schedule,
        meta: { title: '日程管理', icon: 'Calendar' }
      },
      {
        path: 'expense',
        name: 'Expense',
        component: Expense,
        meta: { title: '报销管理', icon: 'Money' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 解析 JWT Token 获取过期时间
 * @returns {number|null} 过期时间戳（秒），解析失败返回 null
 */
function getTokenExpiration(token) {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const payload = JSON.parse(atob(parts[1]))
    return payload.exp || null
  } catch {
    return null
  }
}

/**
 * 检查 token 是否有效
 */
function isTokenValid(token) {
  if (!token) return false
  const exp = getTokenExpiration(token)
  if (!exp) return false
  // 提前30秒判定过期，避免临界情况
  return exp * 1000 > Date.now() + 30000
}

// 路由守卫 — 未登录跳转登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const valid = isTokenValid(token)

  if (to.path === '/login') {
    if (valid) {
      next('/dashboard')
    } else {
      // 清理残留的过期 token
      if (token) localStorage.removeItem('token')
      next()
    }
    return
  }

  if (!valid) {
    localStorage.removeItem('token')
    ElMessage.warning('登录已过期，请重新登录')
    next('/login')
    return
  }
  next()
})

export default router
