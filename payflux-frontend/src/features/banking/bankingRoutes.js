export const bankingRoutes = [
  { id: 'dashboard', label: 'Dashboard', title: 'Dashboard', description: 'Your account, wallet, transfers, and recent PayFlux activity.' },
  { id: 'accounts', label: 'Accounts', title: 'Accounts', description: 'View your PayFlux account identity and assigned account number.' },
  { id: 'wallets', label: 'Wallet', title: 'Wallet', description: 'Review balances, ledger entries, and transfer request status.' },
  { id: 'beneficiaries', label: 'Beneficiaries', title: 'Beneficiaries', description: 'Manage saved recipients for faster transfers.' },
  { id: 'transactions', label: 'Statements', title: 'Statements', description: 'Inspect completed transfers and export wallet statements.' },
  { id: 'notifications', label: 'Notifications', title: 'Notifications', description: 'Review account and transfer alerts.' },
  { id: 'settings', label: 'Settings', title: 'Account settings', description: 'Manage profile, password, and recovery options.', hiddenFromNav: true },
  { id: 'audit', label: 'Admin', title: 'Admin console', description: 'Operational review for users, wallets, transfers, and audit records.', adminOnly: true },
]
