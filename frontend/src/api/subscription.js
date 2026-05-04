import request from './request'

export function subscribe(email) {
  return request.post('/subscribe', { email })
}
