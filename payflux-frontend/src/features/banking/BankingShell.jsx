import { FeedbackBanner } from '../../components/FeedbackBanner'
import { UserAccountMenu } from '../../components/UserAccountMenu'
import { ServiceStatus } from '../dashboard/ServiceStatus'
import { NotificationBell } from '../notifications/NotificationBell'
import { BankingNavigation } from './BankingNavigation'
import { BankingRouteRenderer } from './BankingRouteRenderer'

export function BankingShell({ state, actions }) {
  return (
    <div className="banking-layout">
      <BankingNavigation
        activeRoute={state.activeRoute}
        isAdmin={state.isAdmin}
        onRouteChange={actions.setActiveRoute}
      />

      <main className="app-shell">
        <header className="app-header">
          <div>
            <p className="eyebrow">Protected banking workspace</p>
            <h1>{state.currentRoute.title}</h1>
            <p className="header-copy">
              Manage PayFlux accounts, wallet balance, beneficiaries, statements,
              and customer notifications.
            </p>
          </div>

          <div className="user-menu">
            <NotificationBell notifications={state.notifications} />
            <UserAccountMenu
              user={state.currentUser}
              onRefresh={actions.loadDashboard}
              onLogout={actions.handleLogout}
            />
          </div>
        </header>

        <ServiceStatus stats={state.dashboardStats} />

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
