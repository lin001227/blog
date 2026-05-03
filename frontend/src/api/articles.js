import request from './request'

export function getArticles() {
  return request.get('/articles')
}

export function getArticle(id) {
  return request.get(`/articles/${id}`)
}

export function getAdminArticles() {
  return request.get('/admin/articles')
}

export function createArticle(data) {
  return request.post('/admin/articles', data)
}

export function updateArticle(id, data) {
  return request.put(`/admin/articles/${id}`, data)
}

export function deleteArticle(id) {
  return request.delete(`/admin/articles/${id}`)
}
