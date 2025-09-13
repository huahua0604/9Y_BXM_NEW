import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    name: localStorage.getItem('name') || '',
    roles: JSON.parse(localStorage.getItem('roles') || '[]'),
    mustChangePassword: localStorage.getItem('mcp') === 'true'
  }),
  getters: {
    isAuthed: (s) => !!s.token,
    hasRole: (s) => (role) => s.roles.includes(role)
  },
  actions: {
    setAuth({ token, name, roles, mustChangePassword }) {
      this.token = token
      this.name = name
      this.roles = roles || []
      this.mustChangePassword = !!mustChangePassword
      localStorage.setItem('token', token)
      localStorage.setItem('name', name || '')
      localStorage.setItem('roles', JSON.stringify(this.roles))
      localStorage.setItem('mcp', String(this.mustChangePassword))
    },
    logout() {
      this.token = ''
      this.name = ''
      this.roles = []
      this.mustChangePassword = false
      localStorage.removeItem('token')
      localStorage.removeItem('name')
      localStorage.removeItem('roles')
      localStorage.removeItem('mcp')
    }
  }
})