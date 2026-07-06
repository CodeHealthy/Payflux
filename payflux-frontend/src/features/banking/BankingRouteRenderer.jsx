import { AccountsPage } from '../../pages/AccountsPage'
import { AuditRecordsPage } from '../../pages/AuditRecordsPage'
import { BeneficiariesPage } from '../../pages/BeneficiariesPage'
import { DashboardPage } from '../../pages/DashboardPage'
import { NotificationsPage } from '../../pages/NotificationsPage'
import { StatementsPage } from '../../pages/StatementsPage'
import { WalletPage } from '../../pages/WalletPage'

export function BankingRouteRenderer({ state, actions }) {
  const commonPageProps = {
    isLoading: state.isLoading,
  }

  switch (state.activeRoute) {
    case 'accounts':
      return (
        <AccountsPage
          {...commonPageProps}
          account={state.primaryAccount}
          accounts={state.accounts}
          onRefresh={actions.loadDashboard}
        />
      )
    case 'wallets':
      return (
        <WalletPage
          {...commonPageProps}
          walletDashboard={state.walletDashboard}
          onRefresh={actions.loadDashboard}
        />
      )
    case 'beneficiaries':
      return (
        <BeneficiariesPage
          {...commonPageProps}
          beneficiaries={state.beneficiaries}
          isCreatingBeneficiary={state.isCreatingBeneficiary}
          onCreateBeneficiary={actions.handleCreateBeneficiary}
        />
      )
    case 'transactions':
      return (
        <StatementsPage
          {...commonPageProps}
          transactions={state.transactions}
          currentUserId={state.currentUser.id}
        />
      )
    case 'notifications':
      return (
        <NotificationsPage
          {...commonPageProps}
          notifications={state.notifications}
        />
      )
    case 'audit':
      return (
        <AuditRecordsPage
          {...commonPageProps}
          auditRecords={state.auditRecords}
        />
      )
    default:
      return (
        <DashboardPage
          {...commonPageProps}
          account={state.primaryAccount}
          walletDashboard={state.walletDashboard}
          latestTransaction={state.transactions.at(0)}
          transactions={state.transactions}
          notifications={state.notifications}
          currentUserId={state.currentUser.id}
          beneficiaries={state.beneficiaries}
          activeAction={state.activeAction}
          isTransferring={state.isTransferring}
          isDepositing={state.isDepositing}
          isCreatingBeneficiary={state.isCreatingBeneficiary}
          onActionChange={actions.setActiveAction}
          onCopyAccountNumber={actions.handleCopyAccountNumber}
          onPrepareTransfer={actions.handlePrepareTransfer}
          onConfirmTransfer={actions.handleConfirmTransfer}
          onDeposit={actions.handleDeposit}
          onCreateBeneficiary={actions.handleCreateBeneficiary}
        />
      )
  }
}
