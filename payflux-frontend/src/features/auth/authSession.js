import { clearAccessToken, getAccessToken, saveAccessToken } from '../../api/httpClient'

const USER_KEY = 'payflux.user'

export function saveSession(authResponse) {
  saveAccessToken(authResponse.accessToken)
  localStorage.setItem(USER_KEY, JSON.stringify(authResponse.user))
}

export function getStoredSession() {
  const accessToken = getAccessToken()
  const user = readStoredUser()

  if (!accessToken || !user) {
    return null
  }

  return { accessToken, user }
}

export function clearSession() {
  clearAccessToken()
  localStorage.removeItem(USER_KEY)
}

function readStoredUser() {
  try {
    const value = localStorage.getItem(USER_KEY)
    return value ? JSON.parse(value) : null
  } catch {
    return null
  }
}
