import { LogOut, RefreshCw, Settings, ShieldCheck, UserRound } from 'lucide-react'
import { useRef, useState } from 'react'
import { payfluxAssets } from '../assets/payfluxAssets'
import { useCloseOnOutside } from '../utils/useCloseOnOutside'

export function UserAccountMenu({ user, onRefresh, onLogout, onOpenSettings }) {
  const [isOpen, setIsOpen] = useState(false)
  const menuRef = useRef(null)

  useCloseOnOutside(menuRef, isOpen, () => setIsOpen(false))

  function handleRefresh() {
    setIsOpen(false)
    onRefresh()
  }

  function handleLogout() {
    setIsOpen(false)
    onLogout()
  }

  function handleOpenSettings() {
    setIsOpen(false)
    onOpenSettings()
  }

  return (
    <div className="account-menu" ref={menuRef}>
      <button
        className="account-menu-trigger"
        type="button"
        aria-label="Open account menu"
        aria-expanded={isOpen}
        onClick={() => setIsOpen((current) => !current)}
      >
        <UserRound className="account-menu-trigger-icon" size={25} aria-hidden="true" />
      </button>

      {isOpen && (
        <section className="account-menu-popover" aria-label="Account settings">
          <div className="account-menu-profile">
            <img className="user-avatar large" src={payfluxAssets.userAccount.avatar} alt="" />
            <div>
              <strong>{user.fullName}</strong>
              <span>{user.email}</span>
            </div>
          </div>

          <div className="account-menu-meta">
            <span>
              <UserRound size={15} aria-hidden="true" />
              {user.role || 'USER'}
            </span>
            <span>
              <ShieldCheck size={15} aria-hidden="true" />
              Protected session
            </span>
          </div>

          <div className="account-menu-actions">
            <button type="button" onClick={handleOpenSettings}>
              <Settings size={16} aria-hidden="true" />
              Account settings
            </button>
            <button type="button" onClick={handleRefresh}>
              <RefreshCw size={16} aria-hidden="true" />
              Refresh dashboard
            </button>
            <button type="button" className="danger-menu-action" onClick={handleLogout}>
              <LogOut size={16} aria-hidden="true" />
              Logout
            </button>
          </div>
        </section>
      )}
    </div>
  )
}
