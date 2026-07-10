import { useRef, useState } from 'react'
import { FileText, MessageSquareWarning } from 'lucide-react'
import { FormField } from '../../components/FormField'
import { formatDateTime } from '../../utils/formatDateTime'
import { formatMoney } from '../../utils/formatMoney'
import { useCloseOnOutside } from '../../utils/useCloseOnOutside'

const disputeCategories = [
  'Wrong recipient',
  'Duplicate transfer',
  'Amount issue',
  'Unauthorized activity',
  'Other',
]

export function TransactionReceiptPanel({
  transaction,
  currentUserId,
  dispute,
  isSubmittingDispute,
  onOpenDispute,
  onClose,
}) {
  const panelRef = useRef(null)
  const [isDisputeFormOpen, setIsDisputeFormOpen] = useState(false)
  const [category, setCategory] = useState(disputeCategories[0])
  const [message, setMessage] = useState('')
  useCloseOnOutside(panelRef, Boolean(transaction), onClose)

  if (!transaction) {
    return null
  }

  const isIncoming = transaction.receiverUserId === currentUserId
  const canDispute = !isIncoming && transaction.status === 'COMPLETED' && !dispute

  async function copyReference() {
    await navigator.clipboard.writeText(transaction.transactionReference)
  }

  async function handleOpenDispute(event) {
    event.preventDefault()
    const created = await onOpenDispute(transaction.transactionReference, {
      category,
      message,
    })

    if (created) {
      setIsDisputeFormOpen(false)
      setCategory(disputeCategories[0])
      setMessage('')
    }
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

        {dispute && (
          <section className="dispute-status-card">
            <span>Dispute status</span>
            <strong>{disputeStatusLabel(dispute.status)}</strong>
            <p>{dispute.resolutionNote || disputeStatusHelp(dispute.status)}</p>
          </section>
        )}

        {canDispute && !isDisputeFormOpen && (
          <button className="danger-soft-button" type="button" onClick={() => setIsDisputeFormOpen(true)}>
            Report an issue
          </button>
        )}

        {isDisputeFormOpen && (
          <form className="account-form dispute-form" onSubmit={handleOpenDispute}>
            <div className="secure-confirmation-header compact">
              <span className="secure-confirmation-icon danger">
                <MessageSquareWarning size={22} />
              </span>
              <div>
                <p className="eyebrow">Transfer dispute</p>
                <h3>Report this transfer</h3>
              </div>
            </div>

            <FormField label="Issue category" icon={MessageSquareWarning}>
              <select value={category} onChange={(event) => setCategory(event.target.value)}>
                {disputeCategories.map((option) => (
                  <option key={option} value={option}>{option}</option>
                ))}
              </select>
            </FormField>

            <FormField label="What happened?" hint="Include only details needed for operations review." icon={FileText}>
              <textarea
                maxLength="500"
                minLength="12"
                value={message}
                onChange={(event) => setMessage(event.target.value)}
                placeholder="Describe the problem with this transfer"
                required
              />
            </FormField>

            <div className="form-actions">
              <button className="danger-button" type="submit" disabled={isSubmittingDispute}>
                {isSubmittingDispute ? 'Submitting...' : 'Submit dispute'}
              </button>
              <button className="ghost-button" type="button" onClick={() => setIsDisputeFormOpen(false)}>
                Cancel
              </button>
            </div>
          </form>
        )}
      </aside>
    </div>
  )
}

function disputeStatusLabel(status) {
  if (status === 'OPEN') return 'Open'
  if (status === 'UNDER_REVIEW') return 'Under review'
  if (status === 'RESOLVED') return 'Resolved'
  if (status === 'REJECTED') return 'Rejected'
  return status
}

function disputeStatusHelp(status) {
  if (status === 'OPEN') {
    return 'Your request was received. Operations will review the transfer details.'
  }

  if (status === 'UNDER_REVIEW') {
    return 'An operations reviewer is investigating this transfer.'
  }

  if (status === 'RESOLVED') {
    return 'This case was resolved and any approved reversal has been processed.'
  }

  if (status === 'REJECTED') {
    return 'This case was reviewed and no reversal was approved.'
  }

  return 'This dispute is being tracked by PayFlux operations.'
}
