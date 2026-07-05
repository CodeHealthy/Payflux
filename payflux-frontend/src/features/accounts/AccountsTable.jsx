import { EmptyState } from '../../components/EmptyState'
import { formatDateTime } from '../../utils/formatDateTime'

const noStatementsImage = '/assets/empty-states/no-statements.png'

export function AccountsTable({ accounts, isLoading }) {
  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Account service</p>
          <h2>Accounts</h2>
        </div>
      </div>

      {accounts.length === 0 ? (
        <EmptyState
          imageSrc={noStatementsImage}
          isLoading={isLoading}
          loadingText="Loading accounts..."
          text="No accounts have been created yet."
        />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Account number</th>
                <th>Name</th>
                <th>Email</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {accounts.map((account) => (
                <tr key={account.id}>
                  <td>{account.id}</td>
                  <td className="mono-cell">{account.accountNumber || 'Pending'}</td>
                  <td>{account.fullName}</td>
                  <td>{account.email}</td>
                  <td>{formatDateTime(account.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
