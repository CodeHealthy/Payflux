import {
  clearAuthTokens,
  getAccessToken,
  getRefreshToken,
  saveAccessToken,
  saveRefreshToken,
} from '../../api/httpClient'

const USER_KEY = 'payflux.user'

export function saveSession(authResponse) {
  saveAccessToken(authResponse.accessToken)
  saveRefreshToken(authResponse.refreshToken)
  localStorage.setItem(USER_KEY, JSON.stringify(authResponse.user))
}

export function getStoredSession() {
  const accessToken = getAccessToken()
  const refreshToken = getRefreshToken()
  const user = readStoredUser()

  if ((!accessToken && !refreshToken) || !user) {
    return null
  }

  return { accessToken, refreshToken, user }
}

export function clearSession() {
  clearAuthTokens()
  localStorage.removeItem(USER_KEY)
}

export function saveUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

function readStoredUser() {
  try {
    const value = localStorage.getItem(USER_KEY)
    return value ? JSON.parse(value) : null
  } catch {
    return null
  }
}
