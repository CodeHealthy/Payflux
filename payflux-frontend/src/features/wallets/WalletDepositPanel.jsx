import { useState } from 'react'

export function WalletDepositPanel({ isSubmitting, onDeposit }) {
  const [amount, setAmount] = useState('5000')

  async function handleSubmit(event) {
    event.preventDefault()
    await onDeposit({
      amount: Number(amount),
      idempotencyKey: crypto.randomUUID(),
    })
  }

  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Controlled funding</p>
          <h2>Add money</h2>
        </div>
      </div>

      <form className="account-form" onSubmit={handleSubmit}>
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

        <button className="primary-button" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Adding...' : 'Add money'}
        </button>
      </form>
    </section>
  )
}
