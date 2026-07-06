import { useState } from 'react'
import { formatDateTime } from '../../utils/formatDateTime'
import { formatMoney } from '../../utils/formatMoney'

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
  const [verifiedRecipient, setVerifiedRecipient] = useState(null)

  const normalizedReceiverAccountNumber = receiverAccountNumber.trim()
  const hasVerifiedRecipient = verifiedRecipient?.accountNumber === normalizedReceiverAccountNumber

  function handleBeneficiaryChange(event) {
    const accountNumber = event.target.value
    setReceiverAccountNumber(accountNumber)
    setVerifiedRecipient(null)
  }

  function handleReceiverAccountNumberChange(event) {
    setReceiverAccountNumber(event.target.value)
    setVerifiedRecipient(null)
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
      idempotencyKey: crypto.randomUUID(),
    })

    if (transferConfirmation) {
      setConfirmation(transferConfirmation)
      setOtp('')
    }
  }

  async function handleConfirm(event) {
    event.preventDefault()
    const walletDetails = await onConfirmTransfer({
      confirmationId: confirmation.confirmationId,
      idempotencyKey: confirmation.idempotencyKey,
      otp,
    })

    if (walletDetails) {
      setConfirmation(null)
      setReceiverAccountNumber('')
      setAmount('1000')
      setDescription('PayFlux transfer')
      setOtp('')
      setVerifiedRecipient(null)
    }
  }

  function handleCancelConfirmation() {
    setConfirmation(null)
    setOtp('')
  }

  if (confirmation) {
    return (
      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">Review transfer</p>
            <h2>Confirm money transfer</h2>
          </div>
        </div>

        <div className="review-panel">
          <article>
            <span>Recipient</span>
            <strong>{confirmation.receiverName}</strong>
            <small className="mono-cell">{confirmation.receiverAccountNumber}</small>
          </article>
          <article>
            <span>Amount</span>
            <strong>{formatMoney(confirmation.amount, confirmation.currency)}</strong>
          </article>
          <article>
            <span>Expires</span>
            <strong>{formatDateTime(confirmation.expiresAt)}</strong>
          </article>
          <article className="otp-preview">
            <span>Development OTP</span>
            <strong>{confirmation.devOtp}</strong>
          </article>
        </div>

        <form className="account-form" onSubmit={handleConfirm}>
          <label>
            Confirmation code
            <input
              inputMode="numeric"
              maxLength="6"
              pattern="[0-9]{6}"
              value={otp}
              onChange={(event) => setOtp(event.target.value)}
              placeholder="Enter 6-digit OTP"
              required
            />
          </label>

          <div className="form-actions">
            <button className="primary-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Confirming...' : 'Confirm transfer'}
            </button>
            <button className="ghost-button" type="button" onClick={handleCancelConfirmation}>
              Back
            </button>
          </div>
        </form>
      </section>
    )
  }

  return (
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
            onChange={(event) => setAmount(event.target.value)}
            required
          />
        </label>

        <label>
          Description
          <input
            maxLength="120"
            value={description}
            onChange={(event) => setDescription(event.target.value)}
          />
        </label>

        <button className="primary-button" type="submit" disabled={isSubmitting || !hasVerifiedRecipient}>
          {isSubmitting ? 'Sending...' : 'Send money'}
        </button>
      </form>
    </section>
  )
}
