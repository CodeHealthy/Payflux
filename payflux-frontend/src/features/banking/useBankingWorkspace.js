import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  AUTH_REQUIRED_EVENT,
  AuthRequiredError,
} from '../../api/httpClient'
import { logoutUser } from '../../api/authApi'
import {
  confirmWalletTransfer,
  createBeneficiary,
  depositToWallet,
  exportWalletStatement,
  getAccounts,
  getAdminUsers,
  getAuditRecords,
  getAuditSummary,
  getBeneficiaries,
  getNotifications,
  getTransactionDetails,
  getTransactions,
  getWalletDashboard,
  markAllNotificationsRead,
  markNotificationRead,
  prepareWalletTransfer,
  verifyTransferRecipient,
} from '../../api/payfluxApi'
import { clearSession, getStoredSession } from '../auth/authSession'
import { bankingRoutes } from './bankingRoutes'

export function useBankingWorkspace() {
  const [currentUser, setCurrentUser] = useState(() => getStoredSession()?.user || null)
  const [activeRoute, setActiveRoute] = useState('dashboard')
  const [accounts, setAccounts] = useState([])
  const [beneficiaries, setBeneficiaries] = useState([])
  const [notifications, setNotifications] = useState([])
  const [transactions, setTransactions] = useState([])
  const [selectedTransaction, setSelectedTransaction] = useState(null)
  const [adminUsers, setAdminUsers] = useState([])
  const [auditRecords, setAuditRecords] = useState([])
  const [auditSummary, setAuditSummary] = useState(null)
  const [walletDashboard, setWalletDashboard] = useState(null)
  const [isLoading, setIsLoading] = useState(Boolean(currentUser))
  const [isCreatingBeneficiary, setIsCreatingBeneficiary] = useState(false)
  const [isDepositing, setIsDepositing] = useState(false)
  const [isTransferring, setIsTransferring] = useState(false)
  const [isExportingStatement, setIsExportingStatement] = useState(false)
  const [isLoadingTransactionDetails, setIsLoadingTransactionDetails] = useState(false)
  const [isVerifyingRecipient, setIsVerifyingRecipient] = useState(false)
  const [activeAction, setActiveAction] = useState('transfer')
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const resetDashboardState = useCallback(() => {
    setAccounts([])
    setBeneficiaries([])
    setNotifications([])
    setTransactions([])
    setSelectedTransaction(null)
    setAdminUsers([])
    setAuditRecords([])
    setAuditSummary(null)
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
      dashboardRequests.push(getAuditSummary())
      dashboardRequests.push(getAdminUsers())
    } else {
      setAuditRecords([])
      setAuditSummary(null)
      setAdminUsers([])
    }

    const results = await Promise.allSettled(dashboardRequests)

    const [
      accountResult,
      notificationResult,
      beneficiaryResult,
      walletResult,
      transactionResult,
      auditResult,
      auditSummaryResult,
      adminUsersResult,
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
    if (canReadAuditRecords && auditSummaryResult) {
      collectResult(auditSummaryResult, setAuditSummary, failures)
    }
    if (canReadAuditRecords && adminUsersResult) {
      collectResult(adminUsersResult, setAdminUsers, failures)
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

  useEffect(() => {
    if (!successMessage) {
      return undefined
    }

    const timeoutId = window.setTimeout(() => {
      setSuccessMessage('')
    }, 3500)

    return () => window.clearTimeout(timeoutId)
  }, [successMessage])

  useEffect(() => {
    if (!error) {
      return undefined
    }

    const timeoutId = window.setTimeout(() => {
      setError('')
    }, 8000)

    return () => window.clearTimeout(timeoutId)
  }, [error])

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

  async function handleVerifyRecipient(accountNumber) {
    setError('')
    setIsVerifyingRecipient(true)

    try {
      return await verifyTransferRecipient(accountNumber)
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
      return null
    } finally {
      setIsVerifyingRecipient(false)
    }
  }

  async function handleExportStatement(formValues) {
    setError('')
    setSuccessMessage('')
    setIsExportingStatement(true)

    try {
      const csv = await exportWalletStatement(formValues)
      downloadStatement(csv, formValues)
      setSuccessMessage('Statement export downloaded')
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
    } finally {
      setIsExportingStatement(false)
    }
  }

  async function handleViewTransaction(transactionReference) {
    setError('')
    setIsLoadingTransactionDetails(true)

    try {
      const transaction = await getTransactionDetails(transactionReference)
      setSelectedTransaction(transaction)
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
    } finally {
      setIsLoadingTransactionDetails(false)
    }
  }

  async function handleMarkNotificationRead(notificationId) {
    const previousNotifications = notifications

    setNotifications((currentNotifications) => currentNotifications.map((notification) => (
      notification.id === notificationId
        ? { ...notification, unread: false, readAt: notification.readAt || new Date().toISOString() }
        : notification
    )))

    try {
      const updatedNotification = await markNotificationRead(notificationId)
      setNotifications((currentNotifications) => currentNotifications.map((notification) => (
        notification.id === notificationId ? updatedNotification : notification
      )))
    } catch (requestError) {
      setNotifications(previousNotifications)
      handleRequestError(requestError, handleAuthRequired, setError)
    }
  }

  async function handleMarkAllNotificationsRead() {
    if (notifications.every((notification) => !notification.unread)) {
      return
    }

    const previousNotifications = notifications

    setNotifications((currentNotifications) => currentNotifications.map((notification) => ({
      ...notification,
      unread: false,
      readAt: notification.readAt || new Date().toISOString(),
    })))

    try {
      const updatedNotifications = await markAllNotificationsRead()
      setNotifications(updatedNotifications)
    } catch (requestError) {
      setNotifications(previousNotifications)
      handleRequestError(requestError, handleAuthRequired, setError)
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

  async function handleLogout() {
    try {
      await logoutUser()
    } catch {
      // Local logout must still complete even if the network is unavailable.
    }

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
  const currentRoute = bankingRoutes.find((route) => route.id === activeRoute) || bankingRoutes[0]
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
      selectedTransaction,
      adminUsers,
      auditRecords,
      auditSummary,
      walletDashboard,
      primaryAccount,
      dashboardStats,
      activeAction,
      isLoading,
      isCreatingBeneficiary,
      isDepositing,
      isTransferring,
      isExportingStatement,
      isLoadingTransactionDetails,
      isVerifyingRecipient,
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
      handleVerifyRecipient,
      handleExportStatement,
      handleViewTransaction,
      handleMarkNotificationRead,
      handleMarkAllNotificationsRead,
      closeTransactionDetails: () => setSelectedTransaction(null),
      dismissFeedback: () => {
        setError('')
        setSuccessMessage('')
      },
    },
  }
}

function downloadStatement(csv, { from, to }) {
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const downloadUrl = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = downloadUrl
  anchor.download = `payflux-statement-${from || 'latest'}-to-${to || 'today'}.csv`
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(downloadUrl)
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
