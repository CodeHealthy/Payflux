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

export function getTransferLimits() {
  return request('/api/wallets/transfer-limits')
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

export function resendWalletTransferOtp(confirmationId) {
  return request(`/api/wallets/transfers/confirmations/${encodeURIComponent(confirmationId)}/resend`, {
    method: 'POST',
  })
}

export function confirmWalletTransfer(payload) {
  return request('/api/wallets/transfers/confirm', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getTransferDisputes() {
  return request('/api/wallets/transfers/disputes')
}

export function openTransferDispute(transactionReference, payload) {
  return request(`/api/wallets/transfers/${encodeURIComponent(transactionReference)}/disputes`, {
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

export function getAuditRecords(filters = {}) {
  const params = new URLSearchParams()

  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      params.set(key, String(value).trim())
    }
  })

  const queryString = params.toString()
  return request(`/api/audit-records${queryString ? `?${queryString}` : ''}`)
}

export function getAuditSummary() {
  return request('/api/audit-records/summary')
}

export function getAdminUsers() {
  return request('/api/auth/admin/users')
}

export function getAdminWallets() {
  return request('/api/wallets/admin')
}

export function getAdminTransferActivities() {
  return request('/api/wallets/admin/transfers')
}

export function getAdminTransferDisputes() {
  return request('/api/wallets/admin/disputes')
}

export function markAdminTransferDisputeUnderReview(disputeId) {
  return request(`/api/wallets/admin/disputes/${disputeId}/review`, {
    method: 'POST',
  })
}

export function rejectAdminTransferDispute(disputeId, resolutionNote) {
  return request(`/api/wallets/admin/disputes/${disputeId}/reject`, {
    method: 'POST',
    body: JSON.stringify({ resolutionNote }),
  })
}

export function resolveAdminTransferDispute(disputeId, resolutionNote) {
  return request(`/api/wallets/admin/disputes/${disputeId}/resolve`, {
    method: 'POST',
    body: JSON.stringify({ resolutionNote }),
  })
}

export function reverseAdminTransfer(transactionReference, reason) {
  return request(`/api/wallets/admin/transfers/${encodeURIComponent(transactionReference)}/reverse`, {
    method: 'POST',
    body: JSON.stringify({ reason }),
  })
}

export function suspendAdminWallet(ownerUserId, reason) {
  return request(`/api/wallets/admin/users/${ownerUserId}/suspend`, {
    method: 'POST',
    body: JSON.stringify({ reason }),
  })
}

export function activateAdminWallet(ownerUserId, reason) {
  return request(`/api/wallets/admin/users/${ownerUserId}/activate`, {
    method: 'POST',
    body: JSON.stringify({ reason }),
  })
}
