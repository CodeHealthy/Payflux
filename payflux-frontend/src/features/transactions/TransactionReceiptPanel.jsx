import { useRef } from 'react'
import { formatDateTime } from '../../utils/formatDateTime'
import { formatMoney } from '../../utils/formatMoney'
import { useCloseOnOutside } from '../../utils/useCloseOnOutside'

export function TransactionReceiptPanel({ transaction, currentUserId, onClose }) {
  const panelRef = useRef(null)
  useCloseOnOutside(panelRef, Boolean(transaction), onClose)

  if (!transaction) {
    return null
  }

  const isIncoming = transaction.receiverUserId === currentUserId

  async function copyReference() {
    await navigator.clipboard.writeText(transaction.transactionReference)
  }

  return (
    <div className="receipt-backdrop" role="presentation">
      <aside className="receipt-panel" ref={panelRef} aria-label="Transaction receipt">
        <div className="receipt-header">
          <div>
            <p className="eyebrow">Transaction receipt</p>
            <h2>{isIncoming ? 'Money received' : 'Money sent'}</h2>
          </div>
          <button className="ghost-button compact-button" type="button" onClick={onClose}>
            Close
          </button>
        </div>

        <div className="receipt-amount">
          <span>{transaction.status}</span>
          <strong>{formatMoney(transaction.amount, transaction.currency)}</strong>
          <small>{formatDateTime(transaction.completedAt)}</small>
        </div>

        <dl className="receipt-details">
          <div>
            <dt>Reference</dt>
            <dd className="mono-cell">{transaction.transactionReference}</dd>
          </div>
          <div>
            <dt>From account</dt>
            <dd className="mono-cell">{transaction.senderAccountNumber}</dd>
          </div>
          <div>
            <dt>To account</dt>
            <dd className="mono-cell">{transaction.receiverAccountNumber}</dd>
          </div>
          <div>
            <dt>Description</dt>
            <dd>{transaction.description || 'No description provided'}</dd>
          </div>
        </dl>

        <button className="secondary-button" type="button" onClick={copyReference}>
          Copy reference
        </button>
      </aside>
    </div>
  )
}
