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

export function requestPasswordRecovery(payload) {
  return request('/api/auth/forgot-password', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function resetPassword(payload) {
  return request('/api/auth/reset-password', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getCurrentUser() {
  return request('/api/auth/me')
}

export function updateProfile(payload) {
  return request('/api/auth/me/profile', {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
}

export function updatePassword(payload) {
  return request('/api/auth/me/password', {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
}

export function updateSecurityQuestion(payload) {
  return request('/api/auth/me/security-question', {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
}
