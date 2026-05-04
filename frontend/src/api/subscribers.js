import request from './request'

export function getAdminSubscribers() {
  return request.get('/admin/subscribers')
}

export function deleteAdminSubscriber(id) {
  return request.delete(`/admin/subscribers/${id}`)
}
