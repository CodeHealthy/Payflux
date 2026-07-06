const ACCESS_TOKEN_KEY = 'payflux.accessToken'
const REFRESH_TOKEN_KEY = 'payflux.refreshToken'
export const AUTH_REQUIRED_EVENT = 'payflux:auth-required'
const API_BASE_PATH = import.meta.env.VITE_API_BASE_PATH || '/api'
let refreshRequest = null

export class AuthRequiredError extends Error {
  constructor(message = 'Your session has expired. Please sign in again.') {
    super(message)
    this.name = 'AuthRequiredError'
  }
}

export class ApiRequestError extends Error {
  constructor({ message, code, status, path, fieldErrors, correlationId }) {
    super(message)
    this.name = 'ApiRequestError'
    this.code = code
    this.status = status
    this.path = path
    this.fieldErrors = fieldErrors || null
    this.correlationId = correlationId || null
  }
}

export async function request(path, options = {}) {
  return sendRequest(path, options, true, readJson)
}

export async function requestText(path, options = {}) {
  return sendRequest(path, options, true, (response) => response.text())
}

async function sendRequest(path, options = {}, canRefresh, readResponse) {
  const accessToken = getAccessToken()
  const correlationId = createCorrelationId()

  const response = await fetch(apiPath(path), {
    headers: {
      'Content-Type': 'application/json',
      'X-Correlation-Id': correlationId,
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      ...options.headers,
    },
    ...options,
  })

  if (!response.ok) {
    if (response.status === 401) {
      if (canRefresh && shouldAttemptRefresh(path)) {
        const refreshed = await refreshAccessToken()
        if (refreshed) {
          return sendRequest(path, options, false, readResponse)
        }
      }

      if (!isAuthEndpoint(path)) {
        clearAuthTokens()
        window.dispatchEvent(new CustomEvent(AUTH_REQUIRED_EVENT))
        throw new AuthRequiredError()
      }
    }

    if (response.status === 502) {
      throw new Error(serviceUnavailableMessage())
    }

    throw await readError(response, correlationId)
  }

  return readResponse(response)
}

async function readJson(response) {
  if (response.status === 204) {
    return null
  }

  return response.json()
}

async function refreshAccessToken() {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    return false
  }

  if (!refreshRequest) {
    refreshRequest = fetch(apiPath('/api/auth/refresh'), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Correlation-Id': createCorrelationId(),
      },
      body: JSON.stringify({ refreshToken }),
    })
      .then(async (response) => {
        if (!response.ok) {
          return false
        }

        const authResponse = await response.json()
        saveAccessToken(authResponse.accessToken)
        saveRefreshToken(authResponse.refreshToken)
        return true
      })
      .catch(() => false)
      .finally(() => {
        refreshRequest = null
      })
  }

  return refreshRequest
}

async function readError(response, fallbackCorrelationId) {
  const correlationId = response.headers.get('X-Correlation-Id') || fallbackCorrelationId

  try {
    const body = await response.json()
    return new ApiRequestError({
      message: body.message || body.error || `Request failed with status ${response.status}`,
      code: body.code || 'REQUEST_FAILED',
      status: body.status || response.status,
      path: body.path,
      fieldErrors: body.fieldErrors,
      correlationId: body.correlationId || correlationId,
    })
  } catch {
    return new ApiRequestError({
      message: response.statusText || `Request failed with status ${response.status}`,
      code: 'REQUEST_FAILED',
      status: response.status,
      correlationId,
    })
  }
}

function createCorrelationId() {
  if (window.crypto?.randomUUID) {
    return window.crypto.randomUUID()
  }

  return `payflux-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

function serviceUnavailableMessage() {
  return 'API gateway is not reachable. Check VITE_API_PROXY_TARGET and gatewayservice status.'
}

function apiPath(path) {
  if (!path.startsWith('/api')) {
    return path
  }

  return `${API_BASE_PATH}${path.slice('/api'.length)}`
}

function isAuthEndpoint(path) {
  return path.startsWith('/api/auth/')
}

function shouldAttemptRefresh(path) {
  return !['/api/auth/login', '/api/auth/register', '/api/auth/refresh', '/api/auth/logout'].includes(path)
}

export function saveAccessToken(accessToken) {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
}

export function saveRefreshToken(refreshToken) {
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
}

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function clearAuthTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}
