import { formatDateTime } from '../../utils/formatDateTime'
import { formatMoney } from '../../utils/formatMoney'

export function BankingOverview({ account, walletDashboard, latestTransaction, onCopyAccountNumber }) {
  const wallet = walletDashboard?.wallet

  return (
    <section className="banking-overview">
      <div className="overview-balance">
        <p className="eyebrow">PayFlux wallet</p>
        <span>Available balance</span>
        <strong>{formatMoney(wallet?.availableBalance, wallet?.currency || 'PKR')}</strong>
        <div className="overview-meta">
          <small>Status</small>
          <b>{wallet?.status || 'Pending'}</b>
        </div>
      </div>

      <div className="overview-account">
        <p className="eyebrow">Primary account</p>
        <span>Account number</span>
        <strong className="mono-cell">{account?.accountNumber || 'Pending'}</strong>
        <button
          className="ghost-button compact-button"
          type="button"
          disabled={!account?.accountNumber}
          onClick={onCopyAccountNumber}
        >
          Copy account number
        </button>
      </div>

      <div className="overview-account">
        <p className="eyebrow">Latest transfer</p>
        <span>{latestTransaction ? latestTransaction.status : 'No transfers yet'}</span>
        <strong>
          {latestTransaction
            ? formatMoney(latestTransaction.amount, latestTransaction.currency)
            : 'None'}
        </strong>
        <small>
          {latestTransaction ? formatDateTime(latestTransaction.completedAt) : 'Transfers will appear here'}
        </small>
      </div>
    </section>
  )
}
