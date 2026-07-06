import { Bell, CheckCircle2, CircleDollarSign, Landmark, UserPlus } from 'lucide-react'
import { useMemo, useRef, useState } from 'react'
import { useCloseOnOutside } from '../../utils/useCloseOnOutside'
import { formatDateTime } from '../../utils/formatDateTime'

export function NotificationBell({ notifications }) {
  const [isOpen, setIsOpen] = useState(false)
  const bellRef = useRef(null)
  const recentNotifications = useMemo(() => notifications.slice(0, 5), [notifications])
  useCloseOnOutside(bellRef, isOpen, () => setIsOpen(false))

  return (
    <div className="notification-bell" ref={bellRef}>
      <button
        className="icon-button"
        type="button"
        aria-label="Open notifications"
        aria-expanded={isOpen}
        onClick={() => setIsOpen((current) => !current)}
      >
        <Bell size={19} aria-hidden="true" />
        {notifications.length > 0 && <span>{notifications.length}</span>}
      </button>

      {isOpen && (
        <section className="notification-menu" aria-label="Recent notifications">
          <div className="notification-menu-header">
            <strong>Notifications</strong>
            <small>{notifications.length} total</small>
          </div>

          {recentNotifications.length === 0 ? (
            <div className="notification-empty">
              <CheckCircle2 size={22} aria-hidden="true" />
              <span>No notifications yet</span>
            </div>
          ) : (
            <div className="notification-list">
              {recentNotifications.map((notification) => (
                <article className="notification-item" key={notification.id}>
                  <div className="notification-icon">
                    <NotificationTypeIcon message={notification.message} />
                  </div>
                  <div>
                    <strong>{notificationTitle(notification.message)}</strong>
                    <p>{notificationMessageSummary(notification.message)}</p>
                    <small>{formatDateTime(notification.createdAt)}</small>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      )}
    </div>
  )
}

function NotificationTypeIcon({ message }) {
  const normalizedMessage = message.toLowerCase()

  if (normalizedMessage.includes('transfer')) {
    return <CircleDollarSign size={18} aria-hidden="true" />
  }

  if (normalizedMessage.includes('beneficiary')) {
    return <UserPlus size={18} aria-hidden="true" />
  }

  if (normalizedMessage.includes('account')) {
    return <Landmark size={18} aria-hidden="true" />
  }

  return <CheckCircle2 size={18} aria-hidden="true" />
}

function notificationTitle(message) {
  const normalizedMessage = message.toLowerCase()

  if (normalizedMessage.includes('received')) {
    return 'Money received'
  }

  if (normalizedMessage.includes('sent')) {
    return 'Money sent'
  }

  if (normalizedMessage.includes('beneficiary')) {
    return 'Beneficiary update'
  }

  if (normalizedMessage.includes('account')) {
    return 'Account update'
  }

  return 'PayFlux update'
}

function notificationMessageSummary(message) {
  if (message.length <= 92) {
    return message
  }

  return `${message.slice(0, 89)}...`
}
