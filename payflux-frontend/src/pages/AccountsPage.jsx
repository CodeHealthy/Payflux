import { AccountInfoPanel } from '../features/accounts/AccountInfoPanel'
import { AccountsTable } from '../features/accounts/AccountsTable'

export function AccountsPage({ account, accounts, isLoading, onRefresh }) {
  return (
    <section className="workspace-grid detail-section">
      <AccountInfoPanel account={account} isLoading={isLoading} onRefresh={onRefresh} />
      <AccountsTable accounts={accounts} isLoading={isLoading} />
    </section>
  )
}
