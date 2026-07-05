import { EmptyState } from '../../components/EmptyState'
import { formatDateTime } from '../../utils/formatDateTime'
import { formatMoney } from '../../utils/formatMoney'

const emptyImage = '/assets/empty-states/no-statements.png'

export function TransactionsTable({ transactions, currentUserId, isLoading }) {
  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Transaction service</p>
          <h2>Statements</h2>
        </div>
      </div>

      {transactions.length === 0 ? (
        <EmptyState
          imageSrc={emptyImage}
          isLoading={isLoading}
          loadingText="Loading statements..."
          text="No completed transfers have been recorded yet."
        />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Reference</th>
                <th>Direction</th>
                <th>Amount</th>
                <th>From</th>
                <th>To</th>
                <th>Status</th>
                <th>Completed</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((transaction) => {
                const isIncoming = transaction.receiverUserId === currentUserId

                return (
                  <tr key={transaction.id}>
                    <td className="mono-cell">{transaction.transactionReference}</td>
                    <td>{isIncoming ? 'Incoming' : 'Outgoing'}</td>
                    <td>{formatMoney(transaction.amount, transaction.currency)}</td>
                    <td className="mono-cell">{transaction.senderAccountNumber}</td>
                    <td className="mono-cell">{transaction.receiverAccountNumber}</td>
                    <td><span className="status-pill">{transaction.status}</span></td>
                    <td>{formatDateTime(transaction.completedAt)}</td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
