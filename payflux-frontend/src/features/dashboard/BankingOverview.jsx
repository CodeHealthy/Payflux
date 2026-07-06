import { Copy, ShieldCheck } from 'lucide-react'
import { payfluxAssets } from '../../assets/payfluxAssets'
import { formatDateTime } from '../../utils/formatDateTime'
import { formatMoney } from '../../utils/formatMoney'

export function BankingOverview({ account, walletDashboard, latestTransaction, onCopyAccountNumber }) {
  const wallet = walletDashboard?.wallet

  return (
    <section className="banking-overview">
      <div className="overview-balance">
        <div>
          <p className="eyebrow">PayFlux wallet</p>
          <span>Available balance</span>
          <strong>{formatMoney(wallet?.availableBalance, wallet?.currency || 'PKR')}</strong>
        </div>

        <div className="wallet-card-footer">
          <div className="overview-meta">
            <small>Status</small>
            <b>{wallet?.status || 'Pending'}</b>
          </div>
          <div className="overview-meta">
            <small>Currency</small>
            <b>{wallet?.currency || 'PKR'}</b>
          </div>
        </div>
      </div>

      <div className="overview-account account-card">
        <p className="eyebrow">Primary account</p>
        <span>Account number</span>
        <strong className="mono-cell">{account?.accountNumber || 'Pending'}</strong>
        <button
          className="ghost-button compact-button"
          type="button"
          disabled={!account?.accountNumber}
          onClick={onCopyAccountNumber}
        >
          <Copy size={16} aria-hidden="true" />
          Copy number
        </button>
      </div>

      <div className="overview-account security-card">
        <img src={payfluxAssets.security.shield} alt="" />
        <div>
          <p className="eyebrow">Security</p>
          <strong>Protected session</strong>
          <span>JWT gateway checks, OTP transfers, Redis-backed sessions</span>
        </div>
        <span className="status-pill">
          <ShieldCheck size={14} aria-hidden="true" />
          Active
        </span>
      </div>

      <div className="overview-account latest-transfer-card">
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
