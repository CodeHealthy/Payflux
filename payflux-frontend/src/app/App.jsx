import { AuthPage } from '../features/auth/AuthPage'
import { ServiceStatus } from '../features/dashboard/ServiceStatus'
import { NotificationBell } from '../features/notifications/NotificationBell'
import { AccountsPage } from '../pages/AccountsPage'
import { AuditRecordsPage } from '../pages/AuditRecordsPage'
import { BeneficiariesPage } from '../pages/BeneficiariesPage'
import { DashboardPage } from '../pages/DashboardPage'
import { NotificationsPage } from '../pages/NotificationsPage'
import { StatementsPage } from '../pages/StatementsPage'
import { WalletPage } from '../pages/WalletPage'
import './App.css'
import { routes } from './routes'
import { usePayfluxDashboard } from './usePayfluxDashboard'

function App() {
  const { state, actions } = usePayfluxDashboard()

  if (!state.currentUser) {
    return <AuthPage onAuthenticated={actions.handleAuthenticated} />
  }

  return (
    <div className="banking-layout">
      <aside className="sidebar">
        <div className="brand-lockup compact">
          <img
            className="brand-logo"
            src="/assets/logos/payflux-icon-transparent.png"
            alt=""
          />
          <div>
            <strong>PayFlux</strong>
            <span>Banking OS</span>
          </div>
        </div>

        <nav className="sidebar-nav" aria-label="Main navigation">
          {routes
            .filter((route) => !route.adminOnly || state.isAdmin)
            .map((route) => (
              <button
                className={state.activeRoute === route.id ? 'active' : ''}
                key={route.id}
                type="button"
                onClick={() => actions.setActiveRoute(route.id)}
              >
                {route.label}
              </button>
            ))}
        </nav>
      </aside>

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
            <div>
              <strong>{state.currentUser.fullName}</strong>
              <span>{state.currentUser.email}</span>
            </div>
            <button className="secondary-button" type="button" onClick={actions.loadDashboard}>
              Refresh
            </button>
            <button className="ghost-button" type="button" onClick={actions.handleLogout}>
              Logout
            </button>
          </div>
        </header>

        <ServiceStatus stats={state.dashboardStats} />

        {state.error && <div className="alert alert-error">{state.error}</div>}
        {state.successMessage && <div className="alert alert-success">{state.successMessage}</div>}

        {renderActivePage(state, actions)}
      </main>
    </div>
  )
}

function renderActivePage(state, actions) {
  const commonPageProps = {
    isLoading: state.isLoading,
  }

  switch (state.activeRoute) {
    case 'accounts':
      return (
        <AccountsPage
          {...commonPageProps}
          account={state.primaryAccount}
          accounts={state.accounts}
          onRefresh={actions.loadDashboard}
        />
      )
    case 'wallets':
      return (
        <WalletPage
          {...commonPageProps}
          walletDashboard={state.walletDashboard}
          onRefresh={actions.loadDashboard}
        />
      )
    case 'beneficiaries':
      return (
        <BeneficiariesPage
          {...commonPageProps}
          beneficiaries={state.beneficiaries}
          isCreatingBeneficiary={state.isCreatingBeneficiary}
          onCreateBeneficiary={actions.handleCreateBeneficiary}
        />
      )
    case 'transactions':
      return (
        <StatementsPage
          {...commonPageProps}
          transactions={state.transactions}
          currentUserId={state.currentUser.id}
        />
      )
    case 'notifications':
      return (
        <NotificationsPage
          {...commonPageProps}
          notifications={state.notifications}
        />
      )
    case 'audit':
      return (
        <AuditRecordsPage
          {...commonPageProps}
          auditRecords={state.auditRecords}
        />
      )
    default:
      return (
        <DashboardPage
          {...commonPageProps}
          account={state.primaryAccount}
          walletDashboard={state.walletDashboard}
          latestTransaction={state.transactions.at(0)}
          transactions={state.transactions}
          notifications={state.notifications}
          currentUserId={state.currentUser.id}
          beneficiaries={state.beneficiaries}
          activeAction={state.activeAction}
          isTransferring={state.isTransferring}
          isDepositing={state.isDepositing}
          isCreatingBeneficiary={state.isCreatingBeneficiary}
          onActionChange={actions.setActiveAction}
          onCopyAccountNumber={actions.handleCopyAccountNumber}
          onPrepareTransfer={actions.handlePrepareTransfer}
          onConfirmTransfer={actions.handleConfirmTransfer}
          onDeposit={actions.handleDeposit}
          onCreateBeneficiary={actions.handleCreateBeneficiary}
        />
      )
  }
}

export default App
