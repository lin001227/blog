import { defineStore } from 'pinia'
import { login as apiLogin } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('blog-user') || 'null'),
    token: localStorage.getItem('token') || null,
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    displayName: (state) => state.user?.displayName || state.user?.username || '',
    isAdmin: (state) => state.user?.role === 'ADMIN',
  },
  actions: {
    async login(username, password) {
      const res = await apiLogin(username, password)
      const { token, username: uname, displayName, role, email } = res.data
      this.token = token
      this.user = { username: uname, displayName, role, email }
      localStorage.setItem('token', token)
      localStorage.setItem('blog-user', JSON.stringify({ username: uname, displayName, role, email }))
      return res.data
    },
    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('blog-user')
    },
    setUser(user) {
      this.user = user
      localStorage.setItem('blog-user', JSON.stringify(user))
    },
  },
})
