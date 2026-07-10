import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  AUTH_REQUIRED_EVENT,
  AuthRequiredError,
} from '../../api/httpClient'
import {
  logoutUser,
  updatePassword,
  updateProfile,
  updateSecurityQuestion,
} from '../../api/authApi'
import {
  activateAdminWallet,
  confirmWalletTransfer,
  createBeneficiary,
  depositToWallet,
  exportWalletStatement,
  getAccounts,
  getAdminTransferDisputes,
  getAdminUsers,
  getAdminTransferActivities,
  getAdminWallets,
  getAuditRecords,
  getAuditSummary,
  getBeneficiaries,
  getNotifications,
  getTransactionDetails,
  getTransactions,
  getTransferDisputes,
  getTransferLimits,
  getWalletDashboard,
  markAllNotificationsRead,
  markNotificationRead,
  markAdminTransferDisputeUnderReview,
  openTransferDispute,
  prepareWalletTransfer,
  rejectAdminTransferDispute,
  resendWalletTransferOtp,
  resolveAdminTransferDispute,
  reverseAdminTransfer,
  suspendAdminWallet,
  verifyTransferRecipient,
} from '../../api/payfluxApi'
import { clearSession, getStoredSession, saveUser } from '../auth/authSession'
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
  const [adminWallets, setAdminWallets] = useState([])
  const [adminTransferActivities, setAdminTransferActivities] = useState([])
  const [adminTransferDisputes, setAdminTransferDisputes] = useState([])
  const [transferDisputes, setTransferDisputes] = useState([])
  const [auditRecords, setAuditRecords] = useState([])
  const [auditSummary, setAuditSummary] = useState(null)
  const [walletDashboard, setWalletDashboard] = useState(null)
  const [transferLimits, setTransferLimits] = useState(null)
  const [isLoading, setIsLoading] = useState(Boolean(currentUser))
  const [isCreatingBeneficiary, setIsCreatingBeneficiary] = useState(false)
  const [isDepositing, setIsDepositing] = useState(false)
  const [isTransferring, setIsTransferring] = useState(false)
  const [isExportingStatement, setIsExportingStatement] = useState(false)
  const [isLoadingTransactionDetails, setIsLoadingTransactionDetails] = useState(false)
  const [isSearchingAuditRecords, setIsSearchingAuditRecords] = useState(false)
  const [isVerifyingRecipient, setIsVerifyingRecipient] = useState(false)
  const [isResendingTransferOtp, setIsResendingTransferOtp] = useState(false)
  const [isUpdatingSettings, setIsUpdatingSettings] = useState(false)
  const [isSubmittingDispute, setIsSubmittingDispute] = useState(false)
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
    setAdminWallets([])
    setAdminTransferActivities([])
    setAdminTransferDisputes([])
    setTransferDisputes([])
    setAuditRecords([])
    setAuditSummary(null)
    setWalletDashboard(null)
    setTransferLimits(null)
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
      getTransferLimits(),
      getTransferDisputes(),
      getTransactions(),
    ]

    if (canReadAuditRecords) {
      dashboardRequests.push(getAuditRecords())
      dashboardRequests.push(getAuditSummary())
      dashboardRequests.push(getAdminUsers())
      dashboardRequests.push(getAdminWallets())
      dashboardRequests.push(getAdminTransferActivities())
      dashboardRequests.push(getAdminTransferDisputes())
    } else {
      setAuditRecords([])
      setAuditSummary(null)
      setAdminUsers([])
      setAdminWallets([])
      setAdminTransferActivities([])
      setAdminTransferDisputes([])
    }

    const results = await Promise.allSettled(dashboardRequests)

    const [
      accountResult,
      notificationResult,
      beneficiaryResult,
      walletResult,
      transferLimitResult,
      transferDisputeResult,
      transactionResult,
      auditResult,
      auditSummaryResult,
      adminUsersResult,
      adminWalletsResult,
      adminTransferActivitiesResult,
      adminTransferDisputesResult,
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

    if (transferLimitResult.status === 'fulfilled') {
      setTransferLimits(transferLimitResult.value)
    } else {
      setTransferLimits(null)
      if (transferLimitResult.reason.message !== 'Wallet is not ready yet') {
        failures.push(transferLimitResult.reason.message)
      }
    }

    if (transferDisputeResult.status === 'fulfilled') {
      setTransferDisputes(transferDisputeResult.value)
    } else {
      setTransferDisputes([])
      if (transferDisputeResult.reason.message !== 'Wallet is not ready yet') {
        failures.push(transferDisputeResult.reason.message)
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
    if (canReadAuditRecords && adminWalletsResult) {
      collectResult(adminWalletsResult, setAdminWallets, failures)
    }
    if (canReadAuditRecords && adminTransferActivitiesResult) {
      collectResult(adminTransferActivitiesResult, setAdminTransferActivities, failures)
    }
    if (canReadAuditRecords && adminTransferDisputesResult) {
      collectResult(adminTransferDisputesResult, setAdminTransferDisputes, failures)
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
      await refreshTransferLimits()
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
      if (requestError instanceof AuthRequiredError) {
        handleAuthRequired()
        return null
      }

      return {
        error: requestError.message,
        code: requestError.code,
        correlationId: requestError.correlationId,
      }
    } finally {
      setIsTransferring(false)
    }
  }

  async function handleResendTransferOtp(confirmationId) {
    setError('')
    setSuccessMessage('')
    setIsResendingTransferOtp(true)

    try {
      const confirmation = await resendWalletTransferOtp(confirmationId)
      setSuccessMessage('A new confirmation code has been sent to your email')
      return confirmation
    } catch (requestError) {
      if (requestError instanceof AuthRequiredError) {
        handleAuthRequired()
        return null
      }

      return {
        error: requestError.message,
        code: requestError.code,
        correlationId: requestError.correlationId,
      }
    } finally {
      setIsResendingTransferOtp(false)
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

  async function refreshTransferLimits() {
    try {
      setTransferLimits(await getTransferLimits())
    } catch {
      setTransferLimits(null)
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

  async function handleSuspendWallet(ownerUserId, reason) {
    setError('')
    setSuccessMessage('')

    try {
      const updatedWallet = await suspendAdminWallet(ownerUserId, reason)
      setAdminWallets((currentWallets) => upsertWallet(currentWallets, updatedWallet))
      setSuccessMessage(`Wallet ${updatedWallet.accountNumber} suspended`)
      await loadDashboard()
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
    }
  }

  async function handleActivateWallet(ownerUserId, reason) {
    setError('')
    setSuccessMessage('')

    try {
      const updatedWallet = await activateAdminWallet(ownerUserId, reason)
      setAdminWallets((currentWallets) => upsertWallet(currentWallets, updatedWallet))
      setSuccessMessage(`Wallet ${updatedWallet.accountNumber} activated`)
      await loadDashboard()
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
    }
  }

  async function handleReverseTransfer(transactionReference, reason) {
    setError('')
    setSuccessMessage('')

    try {
      await reverseAdminTransfer(transactionReference, reason)
      setSuccessMessage(`Transfer ${transactionReference} reversed`)
      await loadDashboard()
      return true
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
      return false
    }
  }

  async function handleOpenTransferDispute(transactionReference, formValues) {
    setError('')
    setSuccessMessage('')
    setIsSubmittingDispute(true)

    try {
      const dispute = await openTransferDispute(transactionReference, formValues)
      setTransferDisputes((currentDisputes) => [dispute, ...currentDisputes])
      setSuccessMessage('Dispute submitted for operations review')
      await loadDashboard()
      return dispute
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
      return null
    } finally {
      setIsSubmittingDispute(false)
    }
  }

  async function handleMarkDisputeUnderReview(disputeId) {
    setError('')
    setSuccessMessage('')

    try {
      const dispute = await markAdminTransferDisputeUnderReview(disputeId)
      setAdminTransferDisputes((currentDisputes) => upsertDispute(currentDisputes, dispute))
      setSuccessMessage(`Dispute ${disputeId} moved under review`)
      return dispute
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
      return null
    }
  }

  async function handleRejectDispute(disputeId, resolutionNote) {
    setError('')
    setSuccessMessage('')

    try {
      const dispute = await rejectAdminTransferDispute(disputeId, resolutionNote)
      setAdminTransferDisputes((currentDisputes) => upsertDispute(currentDisputes, dispute))
      setSuccessMessage(`Dispute ${disputeId} rejected`)
      await loadDashboard()
      return dispute
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
      return null
    }
  }

  async function handleResolveDispute(disputeId, resolutionNote) {
    setError('')
    setSuccessMessage('')

    try {
      const dispute = await resolveAdminTransferDispute(disputeId, resolutionNote)
      setAdminTransferDisputes((currentDisputes) => upsertDispute(currentDisputes, dispute))
      setSuccessMessage(`Dispute ${disputeId} resolved with reversal`)
      await loadDashboard()
      return dispute
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
      return null
    }
  }

  async function handleSearchAuditRecords(filters) {
    setError('')
    setIsSearchingAuditRecords(true)

    try {
      const records = await getAuditRecords(filters)
      setAuditRecords(records)
      return records
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
      return []
    } finally {
      setIsSearchingAuditRecords(false)
    }
  }

  async function handleUpdateProfile(formValues) {
    setError('')
    setSuccessMessage('')
    setIsUpdatingSettings(true)

    try {
      const updatedUser = await updateProfile(formValues)
      saveUser(updatedUser)
      setCurrentUser(updatedUser)
      setSuccessMessage('Profile updated successfully')
      return updatedUser
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
      return null
    } finally {
      setIsUpdatingSettings(false)
    }
  }

  async function handleUpdatePassword(formValues) {
    setError('')
    setSuccessMessage('')
    setIsUpdatingSettings(true)

    try {
      await updatePassword(formValues)
      clearSession()
      setCurrentUser(null)
      resetDashboardState()
      return true
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
      return false
    } finally {
      setIsUpdatingSettings(false)
    }
  }

  async function handleUpdateSecurityQuestion(formValues) {
    setError('')
    setSuccessMessage('')
    setIsUpdatingSettings(true)

    try {
      const updatedUser = await updateSecurityQuestion(formValues)
      saveUser(updatedUser)
      setCurrentUser(updatedUser)
      setSuccessMessage('Recovery question updated successfully')
      return updatedUser
    } catch (requestError) {
      handleRequestError(requestError, handleAuthRequired, setError)
      return null
    } finally {
      setIsUpdatingSettings(false)
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
      transactions: walletDashboard?.transferActivities?.length || transactions.length,
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
      adminWallets,
      adminTransferActivities,
      adminTransferDisputes,
      transferDisputes,
      auditRecords,
      auditSummary,
      walletDashboard,
      transferLimits,
      primaryAccount,
      dashboardStats,
      activeAction,
      isLoading,
      isCreatingBeneficiary,
      isDepositing,
      isTransferring,
      isExportingStatement,
      isLoadingTransactionDetails,
      isSearchingAuditRecords,
      isVerifyingRecipient,
      isResendingTransferOtp,
      isUpdatingSettings,
      isSubmittingDispute,
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
      handleResendTransferOtp,
      handleVerifyRecipient,
      handleExportStatement,
      handleViewTransaction,
      handleMarkNotificationRead,
      handleMarkAllNotificationsRead,
      handleSuspendWallet,
      handleActivateWallet,
      handleReverseTransfer,
      handleOpenTransferDispute,
      handleMarkDisputeUnderReview,
      handleRejectDispute,
      handleResolveDispute,
      handleSearchAuditRecords,
      handleUpdateProfile,
      handleUpdatePassword,
      handleUpdateSecurityQuestion,
      closeTransactionDetails: () => setSelectedTransaction(null),
      dismissFeedback: () => {
        setError('')
        setSuccessMessage('')
      },
    },
  }
}

function upsertWallet(wallets, updatedWallet) {
  if (wallets.some((wallet) => wallet.ownerUserId === updatedWallet.ownerUserId)) {
    return wallets.map((wallet) => (
      wallet.ownerUserId === updatedWallet.ownerUserId ? updatedWallet : wallet
    ))
  }

  return [updatedWallet, ...wallets]
}

function upsertDispute(disputes, updatedDispute) {
  if (disputes.some((dispute) => dispute.id === updatedDispute.id)) {
    return disputes.map((dispute) => (
      dispute.id === updatedDispute.id ? updatedDispute : dispute
    ))
  }

  return [updatedDispute, ...disputes]
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
