import { EmptyState } from '../../components/EmptyState'
import { payfluxAssets } from '../../assets/payfluxAssets'
import { formatDateTime } from '../../utils/formatDateTime'

const noNotificationsImage = payfluxAssets.emptyStates.notifications

export function NotificationsTable({ notifications, isLoading, onMarkRead, onMarkAllRead }) {
  const unreadCount = notifications.filter((notification) => notification.unread).length

  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Customer alerts</p>
          <h2>Notifications</h2>
        </div>
        {unreadCount > 0 && (
          <button className="ghost-button compact-button" type="button" onClick={onMarkAllRead}>
            Mark all read
          </button>
        )}
      </div>

      {notifications.length === 0 ? (
        <EmptyState
          imageSrc={noNotificationsImage}
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
                <th>Status</th>
                <th>Date</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {notifications.map((notification) => (
                <tr className={notification.unread ? 'notification-row unread' : 'notification-row'} key={notification.id}>
                  <td>{notification.message}</td>
                  <td>
                    <span className={notification.unread ? 'status-pill unread-pill' : 'status-pill'}>
                      {notification.unread ? 'Unread' : 'Read'}
                    </span>
                  </td>
                  <td>{formatDateTime(notification.createdAt)}</td>
                  <td>
                    {notification.unread ? (
                      <button className="ghost-button compact-button" type="button" onClick={() => onMarkRead(notification.id)}>
                        Mark read
                      </button>
                    ) : (
                      <span className="muted-text">Done</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
