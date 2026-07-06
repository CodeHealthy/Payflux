import { EmptyState } from '../../components/EmptyState'
import { payfluxAssets } from '../../assets/payfluxAssets'
import { formatDateTime } from '../../utils/formatDateTime'
import { formatMoney } from '../../utils/formatMoney'

const emptyImage = payfluxAssets.emptyStates.transactions

export function WalletTransactionsTable({ walletDashboard, isLoading }) {
  const transactions = walletDashboard?.transactions || []
  const currency = walletDashboard?.wallet?.currency || 'PKR'

  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Ledger</p>
          <h2>Recent wallet activity</h2>
        </div>
      </div>

      {transactions.length === 0 ? (
        <EmptyState
          imageSrc={emptyImage}
          isLoading={isLoading}
          loadingText="Loading transactions..."
          text="No wallet transactions have been recorded yet."
        />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Reference</th>
                <th>Type</th>
                <th>Amount</th>
                <th>Counterparty</th>
                <th>Status</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((transaction) => {
                const isCredit = transaction.type.includes('CREDIT') || transaction.type === 'DEPOSIT'

                return (
                  <tr key={transaction.id}>
                    <td className="mono-cell">{transaction.transactionReference}</td>
                    <td>
                      <span className={isCredit ? 'direction-pill incoming' : 'direction-pill outgoing'}>
                        <img
                          src={transaction.type === 'DEPOSIT'
                            ? payfluxAssets.transactionIcons.deposit
                            : isCredit
                              ? payfluxAssets.transactionIcons.transferReceived
                              : payfluxAssets.transactionIcons.transferSent}
                          alt=""
                        />
                        {transaction.type}
                      </span>
                    </td>
                    <td>{formatMoney(transaction.amount, currency)}</td>
                    <td className="mono-cell">{transaction.counterpartyAccountNumber || 'None'}</td>
                    <td><span className="status-pill">{transaction.status}</span></td>
                    <td>{formatDateTime(transaction.createdAt)}</td>
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
