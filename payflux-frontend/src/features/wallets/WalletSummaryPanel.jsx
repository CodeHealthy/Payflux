import { EmptyState } from '../../components/EmptyState'
import { payfluxAssets } from '../../assets/payfluxAssets'
import { formatMoney } from '../../utils/formatMoney'

const walletImage = payfluxAssets.emptyStates.wallet

export function WalletSummaryPanel({ walletDashboard, isLoading, onRefresh }) {
  const wallet = walletDashboard?.wallet

  return (
    <section className="panel wallet-summary-panel" id="wallets">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Wallet service</p>
          <h2>Available balance</h2>
        </div>
        <button className="ghost-button compact-button" type="button" onClick={onRefresh}>
          Refresh
        </button>
      </div>

      {!wallet ? (
        <EmptyState
          imageSrc={walletImage}
          isLoading={isLoading}
          loadingText="Loading wallet..."
          text="Wallet setup is pending. It is created automatically after your account number is issued."
        />
      ) : (
        <div className="wallet-balance-card">
          <span>{wallet.currency} wallet</span>
          <strong>{formatMoney(wallet.availableBalance, wallet.currency)}</strong>
          <div>
            <small>Linked account</small>
            <b className="mono-cell">{wallet.accountNumber}</b>
          </div>
          <p className="status-pill">{wallet.status}</p>
        </div>
      )}
    </section>
  )
}
