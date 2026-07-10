import { AccountsPage } from '../../pages/AccountsPage'
import { AuditRecordsPage } from '../../pages/AuditRecordsPage'
import { BeneficiariesPage } from '../../pages/BeneficiariesPage'
import { DashboardPage } from '../../pages/DashboardPage'
import { NotificationsPage } from '../../pages/NotificationsPage'
import { SettingsPage } from '../../pages/SettingsPage'
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
          transferLimits={state.transferLimits}
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
          selectedTransaction={state.selectedTransaction}
          currentUserId={state.currentUser.id}
          transferDisputes={state.transferDisputes}
          isExportingStatement={state.isExportingStatement}
          isLoadingTransactionDetails={state.isLoadingTransactionDetails}
          isSubmittingDispute={state.isSubmittingDispute}
          onExportStatement={actions.handleExportStatement}
          onViewTransaction={actions.handleViewTransaction}
          onOpenTransferDispute={actions.handleOpenTransferDispute}
          onCloseTransactionDetails={actions.closeTransactionDetails}
        />
      )
    case 'notifications':
      return (
        <NotificationsPage
          {...commonPageProps}
          notifications={state.notifications}
          onMarkRead={actions.handleMarkNotificationRead}
          onMarkAllRead={actions.handleMarkAllNotificationsRead}
        />
      )
    case 'settings':
      return (
        <SettingsPage
          {...commonPageProps}
          currentUser={state.currentUser}
          isUpdatingSettings={state.isUpdatingSettings}
          onUpdateProfile={actions.handleUpdateProfile}
          onUpdatePassword={actions.handleUpdatePassword}
          onUpdateSecurityQuestion={actions.handleUpdateSecurityQuestion}
        />
      )
    case 'audit':
      return (
        <AuditRecordsPage
          {...commonPageProps}
          users={state.adminUsers}
          wallets={state.adminWallets}
          transferActivities={state.adminTransferActivities}
          transferDisputes={state.adminTransferDisputes}
          auditRecords={state.auditRecords}
          auditSummary={state.auditSummary}
          isSearchingAuditRecords={state.isSearchingAuditRecords}
          onSearchAuditRecords={actions.handleSearchAuditRecords}
          onSuspendWallet={actions.handleSuspendWallet}
          onActivateWallet={actions.handleActivateWallet}
          onReverseTransfer={actions.handleReverseTransfer}
          onMarkDisputeUnderReview={actions.handleMarkDisputeUnderReview}
          onRejectDispute={actions.handleRejectDispute}
          onResolveDispute={actions.handleResolveDispute}
        />
      )
    default:
      return (
        <DashboardPage
          {...commonPageProps}
          account={state.primaryAccount}
          walletDashboard={state.walletDashboard}
          transferLimits={state.transferLimits}
          latestTransaction={state.transactions.at(0)}
          transactions={state.transactions}
          notifications={state.notifications}
          currentUserId={state.currentUser.id}
          beneficiaries={state.beneficiaries}
          activeAction={state.activeAction}
          isTransferring={state.isTransferring}
          isVerifyingRecipient={state.isVerifyingRecipient}
          isResendingTransferOtp={state.isResendingTransferOtp}
          isDepositing={state.isDepositing}
          isCreatingBeneficiary={state.isCreatingBeneficiary}
          onActionChange={actions.setActiveAction}
          onCopyAccountNumber={actions.handleCopyAccountNumber}
          onPrepareTransfer={actions.handlePrepareTransfer}
          onConfirmTransfer={actions.handleConfirmTransfer}
          onResendTransferOtp={actions.handleResendTransferOtp}
          onVerifyRecipient={actions.handleVerifyRecipient}
          onDeposit={actions.handleDeposit}
          onCreateBeneficiary={actions.handleCreateBeneficiary}
        />
      )
  }
}
