import { request, requestText } from './httpClient'

export function getAccounts() {
  return request('/api/accounts')
}

export function getNotifications() {
  return request('/api/notifications')
}

export function markNotificationRead(notificationId) {
  return request(`/api/notifications/${notificationId}/read`, {
    method: 'PATCH',
  })
}

export function markAllNotificationsRead() {
  return request('/api/notifications/read-all', {
    method: 'PATCH',
  })
}

export function createBeneficiary(payload) {
  return request('/api/beneficiaries', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getBeneficiaries() {
  return request('/api/beneficiaries')
}

export function verifyTransferRecipient(accountNumber) {
  return request(`/api/beneficiaries/verify/${encodeURIComponent(accountNumber)}`)
}

export function getWalletDashboard() {
  return request('/api/wallets/me')
}

export function depositToWallet(payload) {
  return request('/api/wallets/deposits', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function prepareWalletTransfer(payload) {
  return request('/api/wallets/transfers/confirmations', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function confirmWalletTransfer(payload) {
  return request('/api/wallets/transfers/confirm', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getTransactions() {
  return request('/api/transactions')
}

export function getTransactionDetails(transactionReference) {
  return request(`/api/transactions/${encodeURIComponent(transactionReference)}`)
}

export function exportWalletStatement({ from, to }) {
  const params = new URLSearchParams()

  if (from) {
    params.set('from', from)
  }

  if (to) {
    params.set('to', to)
  }

  const queryString = params.toString()
  return requestText(`/api/wallets/statements/export${queryString ? `?${queryString}` : ''}`)
}

export function getAuditRecords() {
  return request('/api/audit-records')
}
