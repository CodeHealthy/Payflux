import { useEffect, useState } from 'react'
import { Clock3, MailCheck, ShieldCheck } from 'lucide-react'
import { formatDateTime } from '../../utils/formatDateTime'
import { formatMoney } from '../../utils/formatMoney'

function createIdempotencyKey() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }

  return `transfer-${Date.now()}-${Math.random().toString(36).slice(2)}`
}

function getSecondsUntil(dateTime) {
  const expiryTime = new Date(dateTime).getTime()
  if (Number.isNaN(expiryTime)) {
    return 0
  }

  return Math.max(0, Math.ceil((expiryTime - Date.now()) / 1000))
}

function formatRemainingTime(totalSeconds) {
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60

  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}

function confirmationErrorMessage(errorResult) {
  if (errorResult.code === 'TRANSFER_CONFIRMATION_EXPIRED') {
    return 'This confirmation code has expired. Cancel and start the transfer again to receive a new code.'
  }

  if (
    errorResult.code === 'INVALID_TRANSFER_CONFIRMATION_CODE'
    || errorResult.code === 'INVALID_TRANSFER_OTP'
    || errorResult.code === 'INVALID_OTP'
  ) {
    return 'The confirmation code is incorrect. Check your email and try again.'
  }

  if (errorResult.code === 'TRANSFER_CONFIRMATION_LOCKED') {
    return 'Too many incorrect attempts. Cancel this transfer and start again to receive a new code.'
  }

  return errorResult.error || 'We could not confirm this transfer. Please review the code and try again.'
}

export function WalletTransferPanel({
  beneficiaries,
  isSubmitting,
  isVerifyingRecipient,
  onPrepareTransfer,
  onConfirmTransfer,
  onVerifyRecipient,
}) {
  const [receiverAccountNumber, setReceiverAccountNumber] = useState('')
  const [amount, setAmount] = useState('1000')
  const [description, setDescription] = useState('PayFlux transfer')
  const [otp, setOtp] = useState('')
  const [confirmation, setConfirmation] = useState(null)
  const [confirmationError, setConfirmationError] = useState('')
  const [secondsRemaining, setSecondsRemaining] = useState(0)
  const [verifiedRecipient, setVerifiedRecipient] = useState(null)
  const [transferIntentKey, setTransferIntentKey] = useState(() => createIdempotencyKey())

  const normalizedReceiverAccountNumber = receiverAccountNumber.trim()
  const hasVerifiedRecipient = verifiedRecipient?.accountNumber === normalizedReceiverAccountNumber
  const isConfirmationExpired = confirmation && secondsRemaining === 0

  useEffect(() => {
    if (!confirmation) {
      return undefined
    }

    setSecondsRemaining(getSecondsUntil(confirmation.expiresAt))
    const timerId = window.setInterval(() => {
      setSecondsRemaining(getSecondsUntil(confirmation.expiresAt))
    }, 1000)

    return () => window.clearInterval(timerId)
  }, [confirmation])

  function handleBeneficiaryChange(event) {
    const accountNumber = event.target.value
    setReceiverAccountNumber(accountNumber)
    setVerifiedRecipient(null)
    resetTransferIntent()
  }

  function handleReceiverAccountNumberChange(event) {
    setReceiverAccountNumber(event.target.value)
    setVerifiedRecipient(null)
    resetTransferIntent()
  }

  function handleAmountChange(event) {
    setAmount(event.target.value)
    resetTransferIntent()
  }

  function handleDescriptionChange(event) {
    setDescription(event.target.value)
    resetTransferIntent()
  }

  function resetTransferIntent() {
    setConfirmation(null)
    setConfirmationError('')
    setOtp('')
    setTransferIntentKey(createIdempotencyKey())
  }

  async function handleVerifyRecipient() {
    if (!normalizedReceiverAccountNumber) {
      return
    }

    const recipient = await onVerifyRecipient(normalizedReceiverAccountNumber)
    if (recipient) {
      setVerifiedRecipient(recipient)
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()
    if (!hasVerifiedRecipient) {
      return
    }

    const transferConfirmation = await onPrepareTransfer({
      receiverAccountNumber: verifiedRecipient.accountNumber,
      amount: Number(amount),
      description,
      idempotencyKey: transferIntentKey,
    })

    if (transferConfirmation) {
      setConfirmation(transferConfirmation)
      setConfirmationError('')
      setOtp('')
    }
  }

  async function handleConfirm(event) {
    event.preventDefault()
    if (!confirmation) {
      return
    }

    const walletDetails = await onConfirmTransfer({
      confirmationId: confirmation.confirmationId,
      idempotencyKey: confirmation.idempotencyKey,
      otp,
    })

    if (walletDetails?.error) {
      setConfirmationError(confirmationErrorMessage(walletDetails))
      if (
        walletDetails.code === 'TRANSFER_CONFIRMATION_EXPIRED'
        || walletDetails.code === 'TRANSFER_CONFIRMATION_LOCKED'
      ) {
        setSecondsRemaining(0)
      }
      return
    }

    if (walletDetails) {
      setConfirmation(null)
      setConfirmationError('')
      setReceiverAccountNumber('')
      setAmount('1000')
      setDescription('PayFlux transfer')
      setOtp('')
      setVerifiedRecipient(null)
      setTransferIntentKey(createIdempotencyKey())
    }
  }

  function handleCancelConfirmation() {
    setConfirmation(null)
    setConfirmationError('')
    setOtp('')
    setTransferIntentKey(createIdempotencyKey())
  }

  return (
    <>
      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">Internal transfer</p>
            <h2>Send money</h2>
          </div>
        </div>

        <form className="account-form" onSubmit={handleSubmit}>
          {beneficiaries.length > 0 && (
            <label>
              Saved beneficiary
              <select value={receiverAccountNumber} onChange={handleBeneficiaryChange}>
                <option value="">Choose beneficiary or type below</option>
                {beneficiaries.map((beneficiary) => (
                  <option key={beneficiary.id} value={beneficiary.beneficiaryAccountNumber}>
                    {beneficiary.nickname} - {beneficiary.beneficiaryAccountNumber}
                  </option>
                ))}
              </select>
            </label>
          )}

          <label>
            Receiver account number
            <input
              value={receiverAccountNumber}
              onChange={handleReceiverAccountNumberChange}
              placeholder="920100000001"
              required
            />
          </label>

          <div className="recipient-verification-actions">
            <button
              className="secondary-button"
              type="button"
              disabled={!normalizedReceiverAccountNumber || isVerifyingRecipient}
              onClick={handleVerifyRecipient}
            >
              {isVerifyingRecipient ? 'Verifying...' : 'Verify recipient'}
            </button>
          </div>

          {hasVerifiedRecipient && (
            <div className="recipient-verification-card">
              <span>Verified PayFlux account</span>
              <strong>{verifiedRecipient.displayName}</strong>
              <small className="mono-cell">{verifiedRecipient.accountNumber}</small>
              <p>
                {verifiedRecipient.savedBeneficiary
                  ? 'This recipient is saved in your beneficiaries.'
                  : 'This recipient is not saved as a beneficiary yet.'}
              </p>
            </div>
          )}

          <label>
            Amount
            <input
              min="1"
              step="0.01"
              type="number"
              value={amount}
              onChange={handleAmountChange}
              required
            />
          </label>

          <label>
            Description
            <input
              maxLength="120"
              value={description}
              onChange={handleDescriptionChange}
            />
          </label>

          <button className="primary-button" type="submit" disabled={isSubmitting || !hasVerifiedRecipient}>
            {isSubmitting ? 'Sending...' : 'Send money'}
          </button>
        </form>
      </section>

      {confirmation && (
        <div className="secure-confirmation-backdrop" role="presentation">
          <section
            aria-labelledby="transfer-confirmation-title"
            aria-modal="true"
            className="secure-confirmation-dialog"
            role="dialog"
          >
            <div className="secure-confirmation-header">
              <span className="secure-confirmation-icon">
                <ShieldCheck size={24} />
              </span>
              <div>
                <p className="eyebrow">Secure confirmation</p>
                <h2 id="transfer-confirmation-title">Approve this transfer</h2>
              </div>
            </div>

            <div className="email-code-status">
              <MailCheck size={22} />
              <div>
                <strong>Code sent to your email</strong>
                <span>Enter the 6-digit code to release this payment.</span>
              </div>
            </div>

            <div className="confirmation-summary">
              <article>
                <span>Recipient</span>
                <strong>{confirmation.receiverName}</strong>
                <small className="mono-cell">{confirmation.receiverAccountNumber}</small>
              </article>
              <article>
                <span>Amount</span>
                <strong>{formatMoney(confirmation.amount, confirmation.currency)}</strong>
              </article>
            </div>

            <div className={isConfirmationExpired ? 'otp-expiry expired' : 'otp-expiry'}>
              <Clock3 size={18} />
              <span>
                {isConfirmationExpired
                  ? 'This code has expired. Cancel and send a new transfer request.'
                  : `Code expires in ${formatRemainingTime(secondsRemaining)}`}
              </span>
              <small>{formatDateTime(confirmation.expiresAt)}</small>
            </div>

            <form className="account-form confirmation-form" onSubmit={handleConfirm}>
              <label>
                Confirmation code
                <input
                  autoFocus
                  inputMode="numeric"
                  maxLength="6"
                  pattern="[0-9]{6}"
                  value={otp}
                  onChange={(event) => {
                    setOtp(event.target.value.replace(/\D/g, '').slice(0, 6))
                    setConfirmationError('')
                  }}
                  placeholder="000000"
                  required
                />
              </label>

              {confirmationError && (
                <div className="confirmation-error" role="alert">
                  {confirmationError}
                </div>
              )}

              <div className="form-actions">
                <button
                  className="primary-button"
                  type="submit"
                  disabled={isSubmitting || isConfirmationExpired || otp.length !== 6}
                >
                  {isSubmitting ? 'Confirming...' : 'Confirm transfer'}
                </button>
                <button className="ghost-button" type="button" onClick={handleCancelConfirmation}>
                  Cancel
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </>
  )
}
