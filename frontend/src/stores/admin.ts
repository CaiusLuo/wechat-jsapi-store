import { defineStore } from 'pinia'
import { adminLogin, clearAdminToken, getAdminToken, saveAdminLoginResult } from '@/api/admin'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    token: getAdminToken() || '',
  }),
  actions: {
    async login(username: string, password: string) {
      const result = await adminLogin({ username, password })
      saveAdminLoginResult(result)
      this.token = result.token
    },
    logout() {
      clearAdminToken()
      this.token = ''
    },
  },
})
