import { payfluxAssets } from '../../assets/payfluxAssets'

export function transferStatusLabel(status) {
  return status?.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase()) || 'Unknown'
}

export function transferStatusTone(status) {
  if (status === 'COMPLETED') return 'success'
  if (status === 'FAILED' || status === 'REVERSED') return 'danger'
  if (status === 'PROCESSING' || status === 'PENDING_CONFIRMATION') return 'warning'
  return ''
}

export function transferStatusIcon(status) {
  if (status === 'FAILED' || status === 'REVERSED') {
    return payfluxAssets.transactionIcons.failedTransfer
  }

  if (status === 'PROCESSING' || status === 'PENDING_CONFIRMATION') {
    return payfluxAssets.transactionIcons.pendingTransfer
  }

  return payfluxAssets.transactionIcons.transferSent
}

export function transferActivityTitle(status) {
  if (status === 'PENDING_CONFIRMATION') return 'Transfer awaiting confirmation'
  if (status === 'PROCESSING') return 'Transfer processing'
  if (status === 'FAILED') return 'Transfer failed'
  if (status === 'REVERSED') return 'Transfer reversed'
  return 'Money sent'
}
