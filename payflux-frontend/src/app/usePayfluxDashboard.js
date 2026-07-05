import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  AUTH_REQUIRED_EVENT,
  AuthRequiredError,
} from '../api/httpClient'
import {
  confirmWalletTransfer,
  createBeneficiary,
  depositToWallet,
  getAccounts,
  getAuditRecords,
  getBeneficiaries,
  getNotifications,
  getTransactions,
  getWalletDashboard,
  prepareWalletTransfer,
} from '../api/payfluxApi'
import { clearSession, getStoredSession } from '../features/auth/authSession'
import { routes } from './routes'

export function usePayfluxDashboard() {
  const [currentUser, setCurrentUser] = useState(() => getStoredSession()?.user || null)
  const [activeRoute, setActiveRoute] = useState('dashboard')
  const [accounts, setAccounts] = useState([])
  const [beneficiaries, setBeneficiaries] = useState([])
  const [notifications, setNotifications] = useState([])
  const [transactions, setTransactions] = useState([])
  const [auditRecords, setAuditRecords] = useState([])
  const [walletDashboard, setWalletDashboard] = useState(null)
  const [isLoading, setIsLoading] = useState(Boolean(currentUser))
  const [isCreatingBeneficiary, setIsCreatingBeneficiary] = useState(false)
  const [isDepositing, setIsDepositing] = useState(false)
  const [isTransferring, setIsTransferring] = useState(false)
  const [activeAction, setActiveAction] = useState('transfer')
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const resetDashboardState = useCallback(() => {
    setAccounts([])
    setBeneficiaries([])
    setNotifications([])
    setTransactions([])
    setAuditRecords([])
    setWalletDashboard(null)
    setError('')
    setSuccessMessage('')
    setActiveAction('transfer')
    setActiveRoute('dashboard')
  }, [])

  const handleAuthRequired = useCallback(() => {
    clearSession()
    setCurrentUser(null)
    resetDashboardState()
  }, [resetDashboardState])

  const loadDashboard = useCallback(async () => {
    if (!getStoredSession()) {
      setIsLoading(false)
      return
    }

    setError('')
    setIsLoading(true)

    const session = getStoredSession()
    const canReadAuditRecords = session?.user?.role === 'ADMIN'
    const dashboardRequests = [
      getAccounts(),
      getNotifications(),
      getBeneficiaries(),
      getWalletDashboard(),
      getTransactions(),
    ]

    if (canReadAuditRecords) {
      dashboardRequests.push(getAuditRecords())
    } else {
      setAuditRecords([])
    }

    const results = await Promise.allSettled(dashboardRequests)

    const [
      accountResult,
      notificationResult,
      beneficiaryResult,
      walletResult,
      transactionResult,
      auditResult,
    ] = results
    const failures = []
    const hasAuthFailure = results.some(
      (result) => result.status === 'rejected' && result.reason instanceof AuthRequiredError,
    )

    if (hasAuthFailure) {
      handleAuthRequired()
      setIsLoading(false)
      return
    }

    collectResult(accountResult, setAccounts, failures)
    collectResult(notificationResult, setNotifications, failures)
    collectResult(beneficiaryResult, setBeneficiaries, failures)

    if (walletResult.status === 'fulfilled') {
      setWalletDashboard(walletResult.value)
    } else {
      setWalletDashboard(null)
      if (walletResult.reason.message !== 'Wallet is not ready yet') {
        failures.push(walletResult.reason.message)
      }
    }

    collectResult(transactionResult, setTransactions, failures)
    if (canReadAuditRecords && auditResult) {
      collectResult(auditResult, setAuditRecords, failures)
    }

    setError([...new Set(failures)].join(' '))
    setIsLoading(false)
  }, [handleAuthRequired])

  useEffect(() => {
    if (currentUser) {
      loadDashboard()
    }
  }, [currentUser, loadDashboard])

  useEffect(() => {
    window.addEventListener(AUTH_REQUIRED_EVENT, handleAuthRequired)

    return () => {
      window.removeEventListener(AUTH_REQUIRED_EVENT, handleAuthRequired)
    }
  }, [handleAuthRequired])

  async function handleCreateBeneficiary(formValues) {
    setError('')
    setSuccessMessage('')
    setIsCreatingBeneficiary(true)

    try {
      const beneficiary = await createBeneficiary(formValues)
      setSuccessMessage(`Saved beneficiary ${beneficiary.nickname}`)
      await loadDashboard()
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
    } finally {
      setIsCreatingBeneficiary(false)
    }
  }

  async function handleDeposit(formValues) {
    setError('')
    setSuccessMessage('')
    setIsDepositing(true)

    try {
      const walletDetails = await depositToWallet(formValues)
      setWalletDashboard(walletDetails)
      setSuccessMessage('Wallet funded successfully')
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
    } finally {
      setIsDepositing(false)
    }
  }

  async function handlePrepareTransfer(formValues) {
    setError('')
    setSuccessMessage('')
    setIsTransferring(true)

    try {
      return await prepareWalletTransfer(formValues)
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
      return null
    } finally {
      setIsTransferring(false)
    }
  }

  async function handleConfirmTransfer(formValues) {
    setError('')
    setSuccessMessage('')
    setIsTransferring(true)

    try {
      const walletDetails = await confirmWalletTransfer(formValues)
      setWalletDashboard(walletDetails)
      setSuccessMessage('Transfer completed successfully')
      await loadDashboard()
      return walletDetails
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
      return null
    } finally {
      setIsTransferring(false)
    }
  }

  function handleAuthenticated(user) {
    setCurrentUser(user)
    setSuccessMessage(`Signed in as ${user.fullName}`)
  }

  async function handleCopyAccountNumber() {
    if (!primaryAccount?.accountNumber) {
      return
    }

    try {
      await navigator.clipboard.writeText(primaryAccount.accountNumber)
      setSuccessMessage('Account number copied')
    } catch {
      setError('Could not copy account number from this browser session.')
    }
  }

  function handleLogout() {
    clearSession()
    setCurrentUser(null)
    resetDashboardState()
  }

  const dashboardStats = useMemo(
    () => ({
      accounts: accounts.length,
      beneficiaries: beneficiaries.length,
      notifications: notifications.length,
      transactions: transactions.length,
      walletBalance: walletDashboard?.wallet?.availableBalance,
      walletCurrency: walletDashboard?.wallet?.currency,
      lastAccount: accounts.at(0)?.createdAt,
    }),
    [accounts, beneficiaries, notifications, transactions, walletDashboard],
  )

  const primaryAccount = accounts.at(0) || null
  const currentRoute = routes.find((route) => route.id === activeRoute) || routes[0]
  const isAdmin = currentUser?.role === 'ADMIN'

  return {
    state: {
      currentUser,
      activeRoute,
      currentRoute,
      accounts,
      beneficiaries,
      notifications,
      transactions,
      auditRecords,
      walletDashboard,
      primaryAccount,
      dashboardStats,
      activeAction,
      isLoading,
      isCreatingBeneficiary,
      isDepositing,
      isTransferring,
      error,
      successMessage,
      isAdmin,
    },
    actions: {
      setActiveRoute,
      setActiveAction,
      loadDashboard,
      handleAuthenticated,
      handleCopyAccountNumber,
      handleLogout,
      handleCreateBeneficiary,
      handleDeposit,
      handlePrepareTransfer,
      handleConfirmTransfer,
    },
  }
}

function collectResult(result, setter, failures) {
  if (result.status === 'fulfilled') {
    setter(result.value)
    return
  }

  failures.push(result.reason.message)
}

function handleRequestError(error, onAuthRequired, setError) {
  if (error instanceof AuthRequiredError) {
    onAuthRequired()
    return
  }

  setError(error.message)
}
