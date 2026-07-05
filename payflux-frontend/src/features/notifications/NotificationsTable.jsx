import { EmptyState } from '../../components/EmptyState'
import { formatDateTime } from '../../utils/formatDateTime'

const noTransactionsImage = '/assets/empty-states/no-transactions.png'

export function NotificationsTable({ notifications, isLoading }) {
  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Customer alerts</p>
          <h2>Notifications</h2>
        </div>
      </div>

      {notifications.length === 0 ? (
        <EmptyState
          imageSrc={noTransactionsImage}
          isLoading={isLoading}
          loadingText="Loading notifications..."
          text="No customer notifications yet."
        />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Message</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {notifications.map((notification) => (
                <tr key={notification.id}>
                  <td>{notification.message}</td>
                  <td>{formatDateTime(notification.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
