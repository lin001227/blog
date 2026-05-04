import request from './request'

export function getLanguageRankings() {
  return request.get('/language-rankings')
}

export function getAdminLanguageRankings() {
  return request.get('/admin/language-rankings')
}

export function createLanguageRanking(data) {
  return request.post('/admin/language-rankings', data)
}

export function updateLanguageRanking(id, data) {
  return request.put(`/admin/language-rankings/${id}`, data)
}

export function deleteLanguageRanking(id) {
  return request.delete(`/admin/language-rankings/${id}`)
}

export function reorderLanguageRankings(orderList) {
  return request.post('/admin/language-rankings/reorder', orderList)
}
