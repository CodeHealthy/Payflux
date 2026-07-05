import { formatDateTime } from '../../utils/formatDateTime'
import { formatMoney } from '../../utils/formatMoney'

export function ServiceStatus({ stats }) {
  return (
    <section className="status-grid" aria-label="Service overview">
      <article className="stat-card">
        <span>Accounts</span>
        <strong>{stats.accounts}</strong>
      </article>
      <article className="stat-card">
        <span>Notifications</span>
        <strong>{stats.notifications}</strong>
      </article>
      <article className="stat-card">
        <span>Beneficiaries</span>
        <strong>{stats.beneficiaries}</strong>
      </article>
      <article className="stat-card">
        <span>Wallet balance</span>
        <strong>{formatMoney(stats.walletBalance, stats.walletCurrency || 'PKR')}</strong>
      </article>
      <article className="stat-card">
        <span>Transfers</span>
        <strong>{stats.transactions}</strong>
      </article>
      <article className="stat-card">
        <span>Latest account</span>
        <strong>{stats.lastAccount ? formatDateTime(stats.lastAccount) : 'None'}</strong>
      </article>
    </section>
  )
}
