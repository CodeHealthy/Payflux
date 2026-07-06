import { BeneficiaryCreationPanel } from '../features/beneficiaries/BeneficiaryCreationPanel'
import { BankingOverview } from '../features/dashboard/BankingOverview'
import { QuickActions } from '../features/dashboard/QuickActions'
import { RecentActivity } from '../features/dashboard/RecentActivity'
import { WalletDepositPanel } from '../features/wallets/WalletDepositPanel'
import { WalletTransferPanel } from '../features/wallets/WalletTransferPanel'
import '../styles/DashboardPage.css'

export function DashboardPage({
  account,
  walletDashboard,
  latestTransaction,
  transactions,
  notifications,
  currentUserId,
  beneficiaries,
  activeAction,
  isLoading,
  isTransferring,
  isDepositing,
  isCreatingBeneficiary,
  onActionChange,
  onCopyAccountNumber,
  onPrepareTransfer,
  onConfirmTransfer,
  onDeposit,
  onCreateBeneficiary,
}) {
  return (
    <>
      <BankingOverview
        account={account}
        walletDashboard={walletDashboard}
        latestTransaction={latestTransaction}
        onCopyAccountNumber={onCopyAccountNumber}
      />

      <QuickActions activeAction={activeAction} onSelectAction={onActionChange} />

      <section className="dashboard-workspace">
        <div className="action-workspace" aria-live="polite">
          {activeAction === 'transfer' && (
            <WalletTransferPanel
              beneficiaries={beneficiaries}
              isSubmitting={isTransferring}
              onPrepareTransfer={onPrepareTransfer}
              onConfirmTransfer={onConfirmTransfer}
            />
          )}
          {activeAction === 'deposit' && (
            <WalletDepositPanel isSubmitting={isDepositing} onDeposit={onDeposit} />
          )}
          {activeAction === 'beneficiary' && (
            <BeneficiaryCreationPanel
              isSubmitting={isCreatingBeneficiary}
              onCreateBeneficiary={onCreateBeneficiary}
            />
          )}
        </div>

        <RecentActivity
          transactions={transactions}
          notifications={notifications}
          currentUserId={currentUserId}
          isLoading={isLoading}
        />
      </section>
    </>
  )
}
