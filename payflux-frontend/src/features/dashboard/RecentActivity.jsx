import { EmptyState } from '../../components/EmptyState'
import { formatDateTime } from '../../utils/formatDateTime'
import { formatMoney } from '../../utils/formatMoney'

const emptyImage = '/assets/empty-states/no-transactions.png'

export function RecentActivity({ transactions, notifications, currentUserId, isLoading }) {
  const items = [
    ...transactions.slice(0, 5).map((transaction) => ({
      id: `transaction-${transaction.id}`,
      title: transaction.receiverUserId === currentUserId ? 'Money received' : 'Money sent',
      detail: `${transaction.senderAccountNumber} to ${transaction.receiverAccountNumber}`,
      amount: formatMoney(transaction.amount, transaction.currency),
      tone: transaction.receiverUserId === currentUserId ? 'positive' : 'negative',
      createdAt: transaction.completedAt,
    })),
    ...notifications.slice(0, 3).map((notification) => ({
      id: `notification-${notification.id}`,
      title: 'Notification',
      detail: notification.message,
      amount: '',
      tone: 'neutral',
      createdAt: notification.createdAt,
    })),
  ]
    .sort((first, second) => new Date(second.createdAt) - new Date(first.createdAt))
    .slice(0, 6)

  return (
    <section className="panel recent-activity-panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Activity</p>
          <h2>Recent activity</h2>
        </div>
      </div>

      {items.length === 0 ? (
        <EmptyState
          imageSrc={emptyImage}
          isLoading={isLoading}
          loadingText="Loading activity..."
          text="Your recent transfers and service notifications will appear here."
        />
      ) : (
        <div className="activity-list">
          {items.map((item) => (
            <article className="activity-item" key={item.id}>
              <div>
                <strong>{item.title}</strong>
                <span>{item.detail}</span>
              </div>
              <aside>
                {item.amount && <b className={`money-${item.tone}`}>{item.amount}</b>}
                <small>{formatDateTime(item.createdAt)}</small>
              </aside>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
