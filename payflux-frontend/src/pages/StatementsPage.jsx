import { TransactionsTable } from '../features/transactions/TransactionsTable'

export function StatementsPage({ transactions, currentUserId, isLoading }) {
  return (
    <TransactionsTable
      transactions={transactions}
      currentUserId={currentUserId}
      isLoading={isLoading}
    />
  )
}
