import { EmptyState } from '../../components/EmptyState'
import { payfluxAssets } from '../../assets/payfluxAssets'
import { formatDateTime } from '../../utils/formatDateTime'
import { formatMoney } from '../../utils/formatMoney'
import { TransactionReceiptPanel } from './TransactionReceiptPanel'

const emptyImage = payfluxAssets.emptyStates.statements

export function TransactionsTable({
  transactions,
  selectedTransaction,
  currentUserId,
  isLoading,
  isLoadingDetails,
  onViewTransaction,
  onCloseTransactionDetails,
}) {
  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Transaction service</p>
          <h2>Statements</h2>
        </div>
        {isLoadingDetails ? <span className="status-pill">Loading receipt</span> : null}
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
                  <tr
                    className="clickable-row"
                    key={transaction.id}
                    onClick={() => onViewTransaction(transaction.transactionReference)}
                  >
                    <td className="mono-cell">{transaction.transactionReference}</td>
                    <td>
                      <span className={isIncoming ? 'direction-pill incoming' : 'direction-pill outgoing'}>
                        <img
                          src={isIncoming
                            ? payfluxAssets.transactionIcons.transferReceived
                            : payfluxAssets.transactionIcons.transferSent}
                          alt=""
                        />
                        {isIncoming ? 'Incoming' : 'Outgoing'}
                      </span>
                    </td>
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
      <TransactionReceiptPanel
        transaction={selectedTransaction}
        currentUserId={currentUserId}
        onClose={onCloseTransactionDetails}
      />
    </section>
  )
}
