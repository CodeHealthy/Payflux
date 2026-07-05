const ACCESS_TOKEN_KEY = 'payflux.accessToken'
export const AUTH_REQUIRED_EVENT = 'payflux:auth-required'

export class AuthRequiredError extends Error {
  constructor(message = 'Your session has expired. Please sign in again.') {
    super(message)
    this.name = 'AuthRequiredError'
  }
}

export async function request(path, options = {}) {
  const accessToken = getAccessToken()

  const response = await fetch(path, {
    headers: {
      'Content-Type': 'application/json',
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      ...options.headers,
    },
    ...options,
  })

  if (!response.ok) {
    if (response.status === 401 || response.status === 403) {
      clearAccessToken()
      window.dispatchEvent(new CustomEvent(AUTH_REQUIRED_EVENT))
      throw new AuthRequiredError()
    }

    if (response.status === 502) {
      throw new Error(serviceUnavailableMessage(path))
    }

    const message = await readErrorMessage(response)
    throw new Error(message || `Request failed with status ${response.status}`)
  }

  if (response.status === 204) {
    return null
  }

  return response.json()
}

async function readErrorMessage(response) {
  try {
    const body = await response.json()
    return body.message || body.error
  } catch {
    return response.statusText
  }
}

function serviceUnavailableMessage(path) {
  if (path.startsWith('/api/auth')) {
    return 'Auth service is not running on port 8080.'
  }

  if (path.startsWith('/api/accounts')) {
    return 'Account service is not running on port 8081.'
  }

  if (path.startsWith('/api/notifications')) {
    return 'Notification service is not running on port 8082.'
  }

  if (path.startsWith('/api/wallets')) {
    return 'Wallet service is not running on port 8083.'
  }

  if (path.startsWith('/api/beneficiaries')) {
    return 'Beneficiary service is not running on port 8084.'
  }

  if (path.startsWith('/api/transactions')) {
    return 'Transaction service is not running on port 8086.'
  }

  if (path.startsWith('/api/audit-records')) {
    return 'Audit service is not running on port 8087.'
  }

  return 'Backend service is not reachable.'
}

export function saveAccessToken(accessToken) {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
}

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function clearAccessToken() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
}
