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
  transferDisputes = [],
  isLoading,
  isLoadingDetails,
  isSubmittingDispute,
  onViewTransaction,
  onOpenTransferDispute,
  onCloseTransactionDetails,
}) {
  const disputesByReference = new Map(transferDisputes.map((dispute) => [dispute.transactionReference, dispute]))

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
          <table className="transaction-card-table">
            <thead>
              <tr>
                <th>Reference</th>
                <th>Direction</th>
                <th>Amount</th>
                <th>From</th>
                <th>To</th>
                <th>Status</th>
                <th>Dispute</th>
                <th>Completed</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((transaction) => {
                const isIncoming = transaction.receiverUserId === currentUserId
                const dispute = disputesByReference.get(transaction.transactionReference)

                return (
                  <tr
                    className="clickable-row"
                    key={transaction.id}
                    onClick={() => onViewTransaction(transaction.transactionReference)}
                  >
                    <td className="mono-cell" data-label="Reference">{transaction.transactionReference}</td>
                    <td data-label="Direction">
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
                    <td data-label="Amount">{formatMoney(transaction.amount, transaction.currency)}</td>
                    <td className="mono-cell" data-label="From">{transaction.senderAccountNumber}</td>
                    <td className="mono-cell" data-label="To">{transaction.receiverAccountNumber}</td>
                    <td data-label="Status"><span className="status-pill">{transaction.status}</span></td>
                    <td data-label="Dispute">
                      {dispute ? (
                        <span className="status-pill warning">{dispute.status}</span>
                      ) : isIncoming ? (
                        <span className="muted-cell">Not available</span>
                      ) : (
                        <span className="muted-cell">None</span>
                      )}
                    </td>
                    <td data-label="Completed">{formatDateTime(transaction.completedAt)}</td>
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
        dispute={selectedTransaction ? disputesByReference.get(selectedTransaction.transactionReference) : null}
        isSubmittingDispute={isSubmittingDispute}
        onOpenDispute={onOpenTransferDispute}
        onClose={onCloseTransactionDetails}
      />
    </section>
  )
}
