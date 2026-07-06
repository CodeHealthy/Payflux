import { payfluxAssets } from '../../assets/payfluxAssets'
import { bankingRoutes } from './bankingRoutes'

export function BankingNavigation({ activeRoute, isAdmin, onRouteChange }) {
  return (
    <aside className="sidebar">
      <div className="brand-lockup compact">
        <img
          className="brand-logo"
          src={payfluxAssets.logos.icon}
          alt=""
        />
        <div>
          <strong>PayFlux</strong>
          <span>Banking OS</span>
        </div>
      </div>

      <nav className="sidebar-nav" aria-label="Main navigation">
        {bankingRoutes
          .filter((route) => !route.adminOnly || isAdmin)
          .map((route) => (
            <button
              className={activeRoute === route.id ? 'active' : ''}
              key={route.id}
              type="button"
              onClick={() => onRouteChange(route.id)}
            >
              {route.label}
            </button>
          ))}
      </nav>
    </aside>
  )
}
