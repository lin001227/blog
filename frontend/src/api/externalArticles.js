import request from './request'

export function getExternalArticles() {
  return request.get('/external-articles')
}

export function getAdminExternalArticles() {
  return request.get('/admin/external-articles')
}

export function createExternalArticle(data) {
  return request.post('/admin/external-articles', data)
}

export function updateExternalArticle(id, data) {
  return request.put(`/admin/external-articles/${id}`, data)
}

export function deleteExternalArticle(id) {
  return request.delete(`/admin/external-articles/${id}`)
}

export function refetchExternalArticle(id) {
  return request.post(`/admin/external-articles/${id}/refetch`)
}
