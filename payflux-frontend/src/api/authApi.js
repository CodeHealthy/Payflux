import { getRefreshToken, request } from './httpClient'

export function registerUser(payload) {
  return request('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function loginUser(payload) {
  return request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function logoutUser() {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    return Promise.resolve()
  }

  return request('/api/auth/logout', {
    method: 'POST',
    body: JSON.stringify({ refreshToken }),
  })
}
