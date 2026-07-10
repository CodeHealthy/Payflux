import { DisputeCenter } from '../features/transactions/DisputeCenter'
import { StatementExportPanel } from '../features/transactions/StatementExportPanel'
import { TransactionsTable } from '../features/transactions/TransactionsTable'

export function StatementsPage({
  transactions,
  selectedTransaction,
  currentUserId,
  transferDisputes,
  isLoading,
  isExportingStatement,
  isLoadingTransactionDetails,
  isSubmittingDispute,
  onExportStatement,
  onViewTransaction,
  onOpenTransferDispute,
  onCloseTransactionDetails,
}) {
  return (
    <div className="statements-page">
      <StatementExportPanel
        isExporting={isExportingStatement}
        onExportStatement={onExportStatement}
      />
      <DisputeCenter disputes={transferDisputes} isLoading={isLoading} />
      <TransactionsTable
        transactions={transactions}
        selectedTransaction={selectedTransaction}
        currentUserId={currentUserId}
        transferDisputes={transferDisputes}
        isLoading={isLoading}
        isLoadingDetails={isLoadingTransactionDetails}
        isSubmittingDispute={isSubmittingDispute}
        onViewTransaction={onViewTransaction}
        onOpenTransferDispute={onOpenTransferDispute}
        onCloseTransactionDetails={onCloseTransactionDetails}
      />
    </div>
  )
}
