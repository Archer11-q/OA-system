import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi, getUserInfoApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const roles = ref([])
  const permissions = ref([])

  // 登录
  async function login(username, password) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    // 获取用户信息（失败时清理 token，避免半登录状态）
    try {
      await getUserInfo()
    } catch (e) {
      // 回滚：清除已保存的 token
      token.value = ''
      localStorage.removeItem('token')
      throw e
    }
    return res
  }

  // 获取用户信息
  async function getUserInfo() {
    const res = await getUserInfoApi()
    userInfo.value = res.data
    roles.value = res.data.roles || []
    permissions.value = res.data.permissions || []
    return res
  }

  // 登出
  function logout() {
    token.value = ''
    userInfo.value = null
    roles.value = []
    permissions.value = []
    localStorage.removeItem('token')
  }

  return { token, userInfo, roles, permissions, login, getUserInfo, logout }
})
