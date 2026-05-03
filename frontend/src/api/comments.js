import api from './request'

export const getArticleComments = (articleId) => api.get(`/articles/${articleId}/comments`)
export const createComment = (data) => api.post('/comments', data)
export const getAdminComments = () => api.get('/admin/comments')
export const getPendingCount = () => api.get('/admin/comments/pending-count')
export const approveComment = (id) => api.put(`/admin/comments/${id}/approve`)
export const rejectComment = (id) => api.put(`/admin/comments/${id}/reject`)
export const deleteComment = (id) => api.delete(`/admin/comments/${id}`)
export const pinComment = (id) => api.put(`/admin/comments/${id}/pin`)