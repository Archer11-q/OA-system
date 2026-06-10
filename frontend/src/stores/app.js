import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const currentRoute = ref('')

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setCurrentRoute(path) {
    currentRoute.value = path
  }

  return { sidebarCollapsed, currentRoute, toggleSidebar, setCurrentRoute }
})
