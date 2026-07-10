import { CheckCircle2, Circle, Clock3, ShieldAlert, XCircle } from 'lucide-react'
import { EmptyState } from '../../components/EmptyState'
import { payfluxAssets } from '../../assets/payfluxAssets'
import { formatDateTime } from '../../utils/formatDateTime'
import { formatMoney } from '../../utils/formatMoney'

const disputeSteps = ['OPEN', 'UNDER_REVIEW', 'RESOLVED']

export function DisputeCenter({ disputes = [], isLoading }) {
  return (
    <section className="panel dispute-center">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Case tracking</p>
          <h2>Dispute center</h2>
        </div>
      </div>

      {disputes.length === 0 ? (
        <EmptyState
          imageSrc={payfluxAssets.emptyStates.transactions}
          isLoading={isLoading}
          loadingText="Loading disputes..."
          text="Transfer disputes you submit will appear here with their review status."
        />
      ) : (
        <div className="dispute-list">
          {disputes.map((dispute) => (
            <article className="dispute-card" key={dispute.id}>
              <div className="dispute-card-header">
                <span className="dispute-card-icon">
                  {dispute.status === 'REJECTED' ? <XCircle size={20} /> : <ShieldAlert size={20} />}
                </span>
                <div>
                  <p className="eyebrow">{dispute.category}</p>
                  <h3>{dispute.transactionReference}</h3>
                  <small>{formatMoney(dispute.amount, dispute.currency)} to {dispute.receiverAccountNumber}</small>
                </div>
                <span className={`status-pill ${disputeStatusTone(dispute.status)}`}>{statusLabel(dispute.status)}</span>
              </div>

              <DisputeTimeline status={dispute.status} />

              <dl className="dispute-meta">
                <div>
                  <dt>Submitted</dt>
                  <dd>{formatDateTime(dispute.createdAt)}</dd>
                </div>
                <div>
                  <dt>Last updated</dt>
                  <dd>{formatDateTime(dispute.updatedAt)}</dd>
                </div>
                {dispute.resolutionNote && (
                  <div>
                    <dt>Resolution note</dt>
                    <dd>{dispute.resolutionNote}</dd>
                  </div>
                )}
              </dl>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}

function DisputeTimeline({ status }) {
  if (status === 'REJECTED') {
    return (
      <div className="dispute-timeline rejected">
        <TimelineStep label="Opened" isComplete />
        <TimelineStep label="Reviewed" isComplete />
        <TimelineStep label="Rejected" isComplete icon={<XCircle size={16} />} />
      </div>
    )
  }

  const activeIndex = Math.max(0, disputeSteps.indexOf(status))
  return (
    <div className="dispute-timeline">
      {disputeSteps.map((step, index) => (
        <TimelineStep
          key={step}
          label={statusLabel(step)}
          isComplete={index <= activeIndex}
          icon={index < activeIndex ? <CheckCircle2 size={16} /> : index === activeIndex ? <Clock3 size={16} /> : <Circle size={16} />}
        />
      ))}
    </div>
  )
}

function TimelineStep({ label, isComplete, icon }) {
  return (
    <span className={isComplete ? 'complete' : ''}>
      {icon || <CheckCircle2 size={16} />}
      {label}
    </span>
  )
}

function statusLabel(status) {
  if (status === 'OPEN') return 'Open'
  if (status === 'UNDER_REVIEW') return 'Under review'
  if (status === 'RESOLVED') return 'Resolved'
  if (status === 'REJECTED') return 'Rejected'
  return status
}

function disputeStatusTone(status) {
  if (status === 'RESOLVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'UNDER_REVIEW') return 'warning'
  return ''
}
