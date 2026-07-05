import { WalletSummaryPanel } from '../features/wallets/WalletSummaryPanel'
import { WalletTransactionsTable } from '../features/wallets/WalletTransactionsTable'

export function WalletPage({ walletDashboard, isLoading, onRefresh }) {
  return (
    <section className="workspace-grid wallet-workspace detail-section">
      <WalletSummaryPanel
        walletDashboard={walletDashboard}
        isLoading={isLoading}
        onRefresh={onRefresh}
      />
      <WalletTransactionsTable walletDashboard={walletDashboard} isLoading={isLoading} />
    </section>
  )
}
