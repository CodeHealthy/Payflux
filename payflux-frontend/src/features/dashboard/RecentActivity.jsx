import { EmptyState } from '../../components/EmptyState'
import { payfluxAssets } from '../../assets/payfluxAssets'
import { formatDateTime } from '../../utils/formatDateTime'
import { formatMoney } from '../../utils/formatMoney'
import {
  transferActivityTitle,
  transferStatusIcon,
  transferStatusLabel,
  transferStatusTone,
} from '../wallets/transferStatus'

const emptyImage = payfluxAssets.emptyStates.transactions

export function RecentActivity({ transactions, transferActivities = [], notifications, currentUserId, isLoading }) {
  const items = [
    ...transferActivities.slice(0, 5).map((transfer) => ({
      id: `transfer-activity-${transfer.id}`,
      title: transferActivityTitle(transfer.status),
      detail: `${transfer.senderAccountNumber} to ${transfer.receiverAccountNumber}`,
      subdetail: transfer.failureReason || transferStatusLabel(transfer.status),
      statusLabel: transferStatusLabel(transfer.status),
      amount: formatMoney(transfer.amount, transfer.currency),
      tone: transfer.status === 'COMPLETED' ? 'negative' : 'neutral',
      statusTone: transferStatusTone(transfer.status),
      icon: transferStatusIcon(transfer.status),
      createdAt: transfer.updatedAt || transfer.createdAt,
    })),
    ...transactions.slice(0, 5).map((transaction) => ({
      id: `transaction-${transaction.id}`,
      title: transaction.receiverUserId === currentUserId ? 'Money received' : 'Money sent',
      detail: `${transaction.senderAccountNumber} to ${transaction.receiverAccountNumber}`,
      amount: formatMoney(transaction.amount, transaction.currency),
      tone: transaction.receiverUserId === currentUserId ? 'positive' : 'negative',
      icon: transaction.receiverUserId === currentUserId
        ? payfluxAssets.transactionIcons.transferReceived
        : payfluxAssets.transactionIcons.transferSent,
      createdAt: transaction.completedAt,
    })),
    ...notifications.slice(0, 3).map((notification) => ({
      id: `notification-${notification.id}`,
      title: 'Notification',
      detail: notification.message,
      subdetail: '',
      statusLabel: '',
      amount: '',
      tone: 'neutral',
      statusTone: '',
      icon: payfluxAssets.security.shield,
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
              <img className="activity-icon" src={item.icon} alt="" />
              <div>
                <strong>{item.title}</strong>
                <span>{item.detail}</span>
                {item.subdetail && <small>{item.subdetail}</small>}
              </div>
              <aside>
                {item.statusTone && <span className={`status-pill ${item.statusTone}`}>{item.statusLabel}</span>}
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
