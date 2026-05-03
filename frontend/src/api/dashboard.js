import api from './request'

export const getDashboard = () => api.get('/admin/dashboard')
