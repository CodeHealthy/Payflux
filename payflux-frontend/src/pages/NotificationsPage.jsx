import { NotificationsTable } from '../features/notifications/NotificationsTable'

export function NotificationsPage({ notifications, isLoading, onMarkRead, onMarkAllRead }) {
  return (
    <NotificationsTable
      notifications={notifications}
      isLoading={isLoading}
      onMarkRead={onMarkRead}
      onMarkAllRead={onMarkAllRead}
    />
  )
}
