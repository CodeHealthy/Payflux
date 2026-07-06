import { StatementExportPanel } from '../features/transactions/StatementExportPanel'
import { TransactionsTable } from '../features/transactions/TransactionsTable'

export function StatementsPage({
  transactions,
  selectedTransaction,
  currentUserId,
  isLoading,
  isExportingStatement,
  isLoadingTransactionDetails,
  onExportStatement,
  onViewTransaction,
  onCloseTransactionDetails,
}) {
  return (
    <div className="statements-page">
      <StatementExportPanel
        isExporting={isExportingStatement}
        onExportStatement={onExportStatement}
      />
      <TransactionsTable
        transactions={transactions}
        selectedTransaction={selectedTransaction}
        currentUserId={currentUserId}
        isLoading={isLoading}
        isLoadingDetails={isLoadingTransactionDetails}
        onViewTransaction={onViewTransaction}
        onCloseTransactionDetails={onCloseTransactionDetails}
      />
    </div>
  )
}
