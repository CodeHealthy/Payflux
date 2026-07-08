import { FeedbackBanner } from '../../components/FeedbackBanner'
import { UserAccountMenu } from '../../components/UserAccountMenu'
import { NotificationBell } from '../notifications/NotificationBell'
import { BankingNavigation } from './BankingNavigation'
import { BankingRouteRenderer } from './BankingRouteRenderer'

export function BankingShell({ state, actions }) {
  function renderUserActions() {
    return (
      <>
        <NotificationBell
          notifications={state.notifications}
          onMarkRead={actions.handleMarkNotificationRead}
          onMarkAllRead={actions.handleMarkAllNotificationsRead}
        />
        <UserAccountMenu
          user={state.currentUser}
          onRefresh={actions.loadDashboard}
          onLogout={actions.handleLogout}
          onOpenSettings={() => actions.setActiveRoute('settings')}
        />
      </>
    )
  }

  return (
    <div className="banking-layout">
      <BankingNavigation
        activeRoute={state.activeRoute}
        isAdmin={state.isAdmin}
        userActions={renderUserActions()}
        onRouteChange={actions.setActiveRoute}
      />

      <main className="app-shell">
        <header className="app-header">
          <div>
            <p className="eyebrow">Protected banking workspace</p>
            <h1>{state.currentRoute.title}</h1>
            <p className="header-copy">
              {state.currentRoute.description}
            </p>
          </div>

          <div className="user-menu">
            {renderUserActions()}
          </div>
        </header>

        <FeedbackBanner
          error={state.error}
          successMessage={state.successMessage}
          onDismiss={actions.dismissFeedback}
        />

        <BankingRouteRenderer state={state} actions={actions} />
      </main>
    </div>
  )
}
