import { WalletSummaryPanel } from '../features/wallets/WalletSummaryPanel'
import { TransferLimitPanel } from '../features/wallets/TransferLimitPanel'
import { WalletTransactionsTable } from '../features/wallets/WalletTransactionsTable'
import '../styles/WalletPage.css'

export function WalletPage({ walletDashboard, transferLimits, isLoading, onRefresh }) {
  return (
    <section className="workspace-grid wallet-workspace detail-section">
      <div className="wallet-side-stack">
        <WalletSummaryPanel
          walletDashboard={walletDashboard}
          isLoading={isLoading}
          onRefresh={onRefresh}
        />
        <TransferLimitPanel transferLimits={transferLimits} />
      </div>
      <WalletTransactionsTable walletDashboard={walletDashboard} isLoading={isLoading} />
    </section>
  )
}
