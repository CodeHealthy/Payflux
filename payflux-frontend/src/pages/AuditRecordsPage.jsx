import { useState } from 'react'
import {
  CalendarDays,
  FileText,
  ListFilter,
  RotateCcw,
  Search,
  Server,
  SlidersHorizontal,
  UserRound,
} from 'lucide-react'
import { EmptyState } from '../components/EmptyState'
import { FormField } from '../components/FormField'
import { payfluxAssets } from '../assets/payfluxAssets'
import { formatDateTime } from '../utils/formatDateTime'
import { formatMoney } from '../utils/formatMoney'
import { transferStatusLabel, transferStatusTone } from '../features/wallets/transferStatus'

const emptyImage = payfluxAssets.admin.auditLog

export function AuditRecordsPage({
  auditRecords,
  auditSummary,
  users,
  wallets,
  transferActivities = [],
  transferDisputes = [],
  isLoading,
  isSearchingAuditRecords,
  onSearchAuditRecords,
  onSuspendWallet,
  onActivateWallet,
  onReverseTransfer,
  onMarkDisputeUnderReview,
  onRejectDispute,
  onResolveDispute,
}) {
  const walletsByOwnerUserId = new Map(wallets.map((wallet) => [wallet.ownerUserId, wallet]))
  const [transferForReview, setTransferForReview] = useState(null)
  const [selectedAuditRecord, setSelectedAuditRecord] = useState(null)
  const [auditFilters, setAuditFilters] = useState({
    action: '',
    actorUserId: '',
    subjectUserId: '',
    sourceService: '',
    keyword: '',
    from: '',
    to: '',
  })
  const actionOptions = uniqueOptions(auditRecords.map((record) => record.action))
  const sourceOptions = uniqueOptions(auditRecords.map((record) => record.sourceService))
  const activeAuditFilters = auditFilterChips(auditFilters)

  function updateAuditFilter(fieldName, value) {
    setAuditFilters((currentFilters) => ({
      ...currentFilters,
      [fieldName]: value,
    }))
  }

  async function handleAuditFilterSubmit(event) {
    event.preventDefault()
    await onSearchAuditRecords(auditFilters)
  }

  async function handleAuditFilterReset() {
    const emptyFilters = {
      action: '',
      actorUserId: '',
      subjectUserId: '',
      sourceService: '',
      keyword: '',
      from: '',
      to: '',
    }
    setAuditFilters(emptyFilters)
    await onSearchAuditRecords(emptyFilters)
  }

  return (
    <div className="admin-console">
      <section className="admin-hero panel">
        <div>
          <p className="eyebrow">Admin only</p>
          <h2>Operations control</h2>
          <p>
            Review customer access, monitor event activity, and inspect the latest
            compliance records across PayFlux services.
          </p>
        </div>

        <img src={payfluxAssets.admin.adminShield} alt="" />
      </section>

      <section className="admin-metrics" aria-label="Admin overview">
        <AdminMetric label="Total audit records" value={auditSummary?.totalRecords ?? 0} />
        <AdminMetric label="Registered users" value={users.length} />
        <AdminMetric label="Completed transfers" value={auditSummary?.transfersCompleted ?? 0} />
        <AdminMetric label="Beneficiaries added" value={auditSummary?.beneficiariesAdded ?? 0} />
        <AdminMetric
          label="Latest compliance event"
          value={auditSummary?.latestRecordAt ? formatDateTime(auditSummary.latestRecordAt) : 'None'}
        />
      </section>

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">Transfer review</p>
            <h2>Recent transfer requests</h2>
          </div>
        </div>

        {transferActivities.length === 0 ? (
          <EmptyState
            imageSrc={payfluxAssets.emptyStates.transactions}
            isLoading={isLoading}
            loadingText="Loading transfer reviews..."
            text="Completed, failed, pending, and reversed transfers will be available here for operational review."
          />
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Reference</th>
                  <th>From</th>
                  <th>To</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>Updated</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {transferActivities.map((transfer) => (
                  <tr key={transfer.id}>
                    <td className="mono-cell">{transfer.transactionReference}</td>
                    <td className="mono-cell">{transfer.senderAccountNumber}</td>
                    <td className="mono-cell">{transfer.receiverAccountNumber}</td>
                    <td>{formatMoney(transfer.amount, transfer.currency)}</td>
                    <td>
                      <span className={`status-pill ${transferStatusTone(transfer.status)}`}>
                        {transferStatusLabel(transfer.status)}
                      </span>
                      {transfer.failureReason && <small className="muted-cell">{transfer.failureReason}</small>}
                    </td>
                    <td>{formatDateTime(transfer.updatedAt || transfer.createdAt)}</td>
                    <td>
                      {transfer.status === 'COMPLETED' ? (
                        <button
                          className="danger-soft-button"
                          type="button"
                          onClick={() => setTransferForReview(transfer)}
                        >
                          Review reversal
                        </button>
                      ) : (
                        <span className="muted-cell">No action</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">Customer disputes</p>
            <h2>Transfer dispute queue</h2>
          </div>
        </div>

        {transferDisputes.length === 0 ? (
          <EmptyState
            imageSrc={payfluxAssets.emptyStates.transactions}
            isLoading={isLoading}
            loadingText="Loading disputes..."
            text="Customer transfer disputes will appear here for operations review."
          />
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Reference</th>
                  <th>Issue</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>Opened</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {transferDisputes.map((dispute) => (
                  <tr key={dispute.id}>
                    <td className="mono-cell">{dispute.transactionReference}</td>
                    <td>
                      <strong>{dispute.category}</strong>
                      <small className="muted-cell">{dispute.message}</small>
                    </td>
                    <td>{formatMoney(dispute.amount, dispute.currency)}</td>
                    <td><span className={`status-pill ${disputeStatusTone(dispute.status)}`}>{dispute.status}</span></td>
                    <td>{formatDateTime(dispute.createdAt)}</td>
                    <td>
                      <AdminDisputeActions
                        dispute={dispute}
                        onMarkDisputeUnderReview={onMarkDisputeUnderReview}
                        onRejectDispute={onRejectDispute}
                        onResolveDispute={onResolveDispute}
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="admin-grid">
        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Identity access</p>
              <h2>User directory</h2>
            </div>
          </div>

          {users.length === 0 ? (
            <EmptyState
              imageSrc={payfluxAssets.admin.compliance}
              isLoading={isLoading}
              loadingText="Loading users..."
              text="No users are available for review."
            />
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>User</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Wallet</th>
                    <th>Balance</th>
                    <th>Created</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((user) => {
                    const wallet = walletsByOwnerUserId.get(user.id)

                    return (
                      <tr key={user.id}>
                        <td>{user.fullName}</td>
                        <td>{user.email}</td>
                        <td><span className="status-pill">{user.role}</span></td>
                        <td>
                          {wallet ? (
                            <div className="admin-wallet-cell">
                              <strong>{wallet.accountNumber}</strong>
                              <span className={`status-pill ${wallet.status === 'ACTIVE' ? 'success' : 'warning'}`}>
                                {wallet.status}
                              </span>
                            </div>
                          ) : (
                            'Not provisioned'
                          )}
                        </td>
                        <td>
                          {wallet
                            ? formatMoney(wallet.availableBalance, wallet.currency)
                            : 'None'}
                        </td>
                        <td>{formatDateTime(user.createdAt)}</td>
                        <td>
                          <AdminWalletAction
                            user={user}
                            wallet={wallet}
                            onSuspendWallet={onSuspendWallet}
                            onActivateWallet={onActivateWallet}
                          />
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Compliance trail</p>
              <h2>Recent audit records</h2>
            </div>
          </div>

          <form className="audit-filter-panel" onSubmit={handleAuditFilterSubmit}>
            <div className="audit-filter-toolbar">
              <div>
                <p className="eyebrow">Search controls</p>
                <h3>Filter audit trail</h3>
              </div>
              <span>{activeAuditFilters.length} active</span>
            </div>

            <FormField label="Action" icon={ListFilter}>
              <select
                value={auditFilters.action}
                onChange={(event) => updateAuditFilter('action', event.target.value)}
              >
                <option value="">All actions</option>
                {actionOptions.map((action) => (
                  <option key={action} value={action}>{action}</option>
                ))}
              </select>
            </FormField>

            <FormField label="Source" icon={Server}>
              <select
                value={auditFilters.sourceService}
                onChange={(event) => updateAuditFilter('sourceService', event.target.value)}
              >
                <option value="">All services</option>
                {sourceOptions.map((sourceService) => (
                  <option key={sourceService} value={sourceService}>{sourceService}</option>
                ))}
              </select>
            </FormField>

            <FormField label="Actor user ID" hint="Leave blank for any actor." icon={UserRound}>
              <input
                inputMode="numeric"
                min="1"
                type="number"
                value={auditFilters.actorUserId}
                onChange={(event) => updateAuditFilter('actorUserId', event.target.value)}
                placeholder="Any"
              />
            </FormField>

            <FormField label="Subject user ID" hint="Use this to inspect one customer." icon={UserRound}>
              <input
                inputMode="numeric"
                min="1"
                type="number"
                value={auditFilters.subjectUserId}
                onChange={(event) => updateAuditFilter('subjectUserId', event.target.value)}
                placeholder="Any"
              />
            </FormField>

            <FormField label="From" icon={CalendarDays}>
              <input
                type="date"
                value={auditFilters.from}
                onChange={(event) => updateAuditFilter('from', event.target.value)}
              />
            </FormField>

            <FormField label="To" icon={CalendarDays}>
              <input
                type="date"
                value={auditFilters.to}
                onChange={(event) => updateAuditFilter('to', event.target.value)}
              />
            </FormField>

            <FormField
              className="audit-keyword-field"
              label="Search"
              hint="Searches summary, source event id, aggregate id, and related text."
              icon={Search}
            >
              <input
                value={auditFilters.keyword}
                onChange={(event) => updateAuditFilter('keyword', event.target.value)}
                placeholder="Summary, event id, aggregate id"
              />
            </FormField>

            {activeAuditFilters.length > 0 && (
              <div className="filter-chip-row" aria-label="Active audit filters">
                {activeAuditFilters.map((filter) => (
                  <button
                    className="filter-chip"
                    key={filter.key}
                    type="button"
                    onClick={() => updateAuditFilter(filter.key, '')}
                  >
                    {filter.label}
                    <span aria-hidden="true">x</span>
                  </button>
                ))}
              </div>
            )}

            <div className="audit-filter-actions">
              <button className="secondary-button icon-text-button" type="submit" disabled={isSearchingAuditRecords}>
                <SlidersHorizontal size={17} />
                {isSearchingAuditRecords ? 'Searching...' : 'Apply filters'}
              </button>
              <button className="ghost-button icon-text-button" type="button" disabled={isSearchingAuditRecords} onClick={handleAuditFilterReset}>
                <RotateCcw size={17} />
                Reset
              </button>
            </div>
          </form>

          {auditRecords.length === 0 ? (
            <EmptyState
              imageSrc={emptyImage}
              isLoading={isLoading}
              loadingText="Loading audit records..."
              text="No audit records have been stored yet."
            />
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Action</th>
                    <th>Summary</th>
                    <th>Actor</th>
                    <th>Subject</th>
                    <th>Source</th>
                    <th>Created</th>
                  </tr>
                </thead>
                <tbody>
                  {auditRecords.map((record) => (
                    <tr className="clickable-row" key={record.id} onClick={() => setSelectedAuditRecord(record)}>
                      <td><span className="status-pill">{record.action}</span></td>
                      <td>{record.summary}</td>
                      <td>{record.actorUserId || 'System'}</td>
                      <td>{record.subjectUserId || 'None'}</td>
                      <td>{record.sourceService}</td>
                      <td>{formatDateTime(record.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </section>

      <AuditRecordDetailsDialog
        record={selectedAuditRecord}
        onClose={() => setSelectedAuditRecord(null)}
      />

      <TransferReversalDialog
        transfer={transferForReview}
        onClose={() => setTransferForReview(null)}
        onReverseTransfer={onReverseTransfer}
      />
    </div>
  )
}

function uniqueOptions(values) {
  return [...new Set(values.filter(Boolean))].sort((left, right) => left.localeCompare(right))
}

function auditFilterChips(filters) {
  return [
    { key: 'action', label: filters.action },
    { key: 'sourceService', label: filters.sourceService },
    { key: 'actorUserId', label: filters.actorUserId ? `Actor ${filters.actorUserId}` : '' },
    { key: 'subjectUserId', label: filters.subjectUserId ? `Subject ${filters.subjectUserId}` : '' },
    { key: 'from', label: filters.from ? `From ${filters.from}` : '' },
    { key: 'to', label: filters.to ? `To ${filters.to}` : '' },
    { key: 'keyword', label: filters.keyword ? `Search: ${filters.keyword}` : '' },
  ].filter((filter) => filter.label)
}

function AdminMetric({ label, value }) {
  return (
    <article className="stat-card">
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  )
}

function AdminWalletAction({ user, wallet, onSuspendWallet, onActivateWallet }) {
  if (!wallet || user.role === 'ADMIN') {
    return <span className="muted-cell">No action</span>
  }

  if (wallet.status === 'ACTIVE') {
    return (
      <button
        className="danger-soft-button"
        type="button"
        onClick={() => requestWalletReason({
          actionLabel: 'Suspend wallet',
          fallbackReason: 'Suspicious activity review',
          onConfirm: (reason) => onSuspendWallet(user.id, reason),
        })}
      >
        Suspend
      </button>
    )
  }

  if (wallet.status === 'SUSPENDED') {
    return (
      <button
        className="compact-button"
        type="button"
        onClick={() => requestWalletReason({
          actionLabel: 'Activate wallet',
          fallbackReason: 'Review completed',
          onConfirm: (reason) => onActivateWallet(user.id, reason),
        })}
      >
        Activate
      </button>
    )
  }

  return <span className="muted-cell">Closed</span>
}

function AdminDisputeActions({
  dispute,
  onMarkDisputeUnderReview,
  onRejectDispute,
  onResolveDispute,
}) {
  if (dispute.status === 'RESOLVED' || dispute.status === 'REJECTED') {
    return <span className="muted-cell">{dispute.resolutionNote || 'Closed'}</span>
  }

  return (
    <div className="admin-action-stack">
      {dispute.status === 'OPEN' && (
        <button
          className="compact-button"
          type="button"
          onClick={() => onMarkDisputeUnderReview(dispute.id)}
        >
          Review
        </button>
      )}
      <button
        className="danger-soft-button"
        type="button"
        onClick={() => requestWalletReason({
          actionLabel: 'Reject dispute',
          fallbackReason: 'Dispute rejected after operations review',
          onConfirm: (reason) => onRejectDispute(dispute.id, reason),
        })}
      >
        Reject
      </button>
      <button
        className="secondary-button compact-button"
        type="button"
        onClick={() => requestWalletReason({
          actionLabel: 'Resolve dispute',
          fallbackReason: 'Dispute resolved with transfer reversal',
          onConfirm: (reason) => onResolveDispute(dispute.id, reason),
        })}
      >
        Resolve
      </button>
    </div>
  )
}

function disputeStatusTone(status) {
  if (status === 'RESOLVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'UNDER_REVIEW') return 'warning'
  return ''
}

function requestWalletReason({ actionLabel, fallbackReason, onConfirm }) {
  const reason = window.prompt(`${actionLabel} reason`, fallbackReason)

  if (reason === null) {
    return
  }

  onConfirm(reason.trim() || fallbackReason)
}

function TransferReversalDialog({ transfer, onClose, onReverseTransfer }) {
  const [reason, setReason] = useState('Customer dispute reviewed by operations')
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (!transfer) {
    return null
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setIsSubmitting(true)
    const reversed = await onReverseTransfer(
      transfer.transactionReference,
      reason.trim() || 'Transfer reversed by operations review',
    )
    setIsSubmitting(false)

    if (reversed) {
      onClose()
      setReason('Customer dispute reviewed by operations')
    }
  }

  return (
    <div className="secure-confirmation-backdrop" role="presentation">
      <section
        aria-labelledby="transfer-reversal-title"
        aria-modal="true"
        className="secure-confirmation-dialog"
        role="dialog"
      >
        <div className="secure-confirmation-header">
          <span className="secure-confirmation-icon danger">!</span>
          <div>
            <p className="eyebrow">Admin review</p>
            <h2 id="transfer-reversal-title">Reverse transfer</h2>
          </div>
        </div>

        <div className="confirmation-summary">
          <article>
            <span>Reference</span>
            <strong className="mono-cell">{transfer.transactionReference}</strong>
          </article>
          <article>
            <span>Amount</span>
            <strong>{formatMoney(transfer.amount, transfer.currency)}</strong>
          </article>
          <article>
            <span>From</span>
            <strong className="mono-cell">{transfer.senderAccountNumber}</strong>
          </article>
          <article>
            <span>To</span>
            <strong className="mono-cell">{transfer.receiverAccountNumber}</strong>
          </article>
        </div>

        <form className="account-form" onSubmit={handleSubmit}>
          <FormField label="Reversal reason" hint="This reason is written into the audit trail." icon={FileText}>
            <textarea
              maxLength="240"
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              required
            />
          </FormField>

          <div className="form-actions">
            <button className="danger-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Reversing...' : 'Reverse transfer'}
            </button>
            <button className="ghost-button" type="button" onClick={onClose}>
              Cancel
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}

function AuditRecordDetailsDialog({ record, onClose }) {
  if (!record) {
    return null
  }

  const payload = parsePayload(record.payload)

  return (
    <div className="secure-confirmation-backdrop" role="presentation">
      <section
        aria-labelledby="audit-record-detail-title"
        aria-modal="true"
        className="secure-confirmation-dialog audit-detail-dialog"
        role="dialog"
      >
        <div className="secure-confirmation-header">
          <span className="secure-confirmation-icon">i</span>
          <div>
            <p className="eyebrow">Audit detail</p>
            <h2 id="audit-record-detail-title">{record.action}</h2>
          </div>
        </div>

        <div className="audit-detail-summary">
          <p>{record.summary}</p>
        </div>

        <dl className="receipt-details">
          <div>
            <dt>Source service</dt>
            <dd>{record.sourceService}</dd>
          </div>
          <div>
            <dt>Source event</dt>
            <dd className="mono-cell">{record.sourceEventId}</dd>
          </div>
          <div>
            <dt>Actor</dt>
            <dd>{record.actorUserId || 'System'}</dd>
          </div>
          <div>
            <dt>Subject</dt>
            <dd>{record.subjectUserId || 'None'}</dd>
          </div>
          <div>
            <dt>Aggregate</dt>
            <dd>{record.aggregateType} / <span className="mono-cell">{record.aggregateId}</span></dd>
          </div>
          <div>
            <dt>Occurred</dt>
            <dd>{formatDateTime(record.eventOccurredAt)}</dd>
          </div>
          <div>
            <dt>Stored</dt>
            <dd>{formatDateTime(record.createdAt)}</dd>
          </div>
        </dl>

        <section className="audit-payload-panel">
          <p className="eyebrow">Sanitized payload</p>
          <pre>{JSON.stringify(payload, null, 2)}</pre>
        </section>

        <div className="form-actions">
          <button className="secondary-button" type="button" onClick={onClose}>
            Close
          </button>
        </div>
      </section>
    </div>
  )
}

function parsePayload(payload) {
  if (!payload) {
    return {}
  }

  try {
    return JSON.parse(payload)
  } catch {
    return { raw: payload }
  }
}
