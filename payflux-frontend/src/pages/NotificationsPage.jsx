import { NotificationsTable } from '../features/notifications/NotificationsTable'

export function NotificationsPage({ notifications, isLoading }) {
  return <NotificationsTable notifications={notifications} isLoading={isLoading} />
}
