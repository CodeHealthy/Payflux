import { EmptyState } from '../../components/EmptyState'
import { payfluxAssets } from '../../assets/payfluxAssets'
import { formatDateTime } from '../../utils/formatDateTime'

const pendingAccountImage = payfluxAssets.emptyStates.wallet

export function AccountInfoPanel({ account, isLoading, onRefresh }) {
  return (
    <section className="panel account-info-panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">My PayFlux account</p>
          <h2>Banking info</h2>
        </div>
        <button className="ghost-button compact-button" type="button" onClick={onRefresh}>
          Refresh
        </button>
      </div>

      {!account ? (
        <EmptyState
          imageSrc={pendingAccountImage}
          isLoading={isLoading}
          loadingText="Loading account..."
          text="Account setup is pending. Refresh after signup if your account number is not visible yet."
        />
      ) : (
        <div className="account-info-grid">
          <article>
            <span>Account number</span>
            <strong className="mono-cell">{account.accountNumber}</strong>
          </article>
          <article>
            <span>Account holder</span>
            <strong>{account.fullName}</strong>
          </article>
          <article>
            <span>Email</span>
            <strong>{account.email}</strong>
          </article>
          <article>
            <span>Opened</span>
            <strong>{formatDateTime(account.createdAt)}</strong>
          </article>
        </div>
      )}
    </section>
  )
}
