import { Gauge, ShieldCheck } from 'lucide-react'
import { formatDateTime } from '../../utils/formatDateTime'
import { formatMoney } from '../../utils/formatMoney'

function usagePercent(used, limit) {
  const usedNumber = Number(used || 0)
  const limitNumber = Number(limit || 0)

  if (limitNumber <= 0) {
    return 0
  }

  return Math.min(100, Math.max(0, Math.round((usedNumber / limitNumber) * 100)))
}

export function TransferLimitPanel({ transferLimits }) {
  if (!transferLimits) {
    return (
      <section className="panel transfer-limit-panel">
        <div className="transfer-limit-heading">
          <span className="transfer-limit-icon"><ShieldCheck size={20} /></span>
          <div>
            <p className="eyebrow">Transfer controls</p>
            <h2>Daily limits</h2>
          </div>
        </div>
        <p className="muted-text">Transfer limits will appear when your wallet is ready.</p>
      </section>
    )
  }

  const amountUsage = usagePercent(
    transferLimits.dailyTransferAmountUsed,
    transferLimits.dailyTransferAmountLimit,
  )
  const countUsage = usagePercent(
    transferLimits.dailyTransferCountUsed,
    transferLimits.dailyTransferCountLimit,
  )

  return (
    <section className="panel transfer-limit-panel">
      <div className="transfer-limit-heading">
        <span className="transfer-limit-icon"><ShieldCheck size={20} /></span>
        <div>
          <p className="eyebrow">Transfer controls</p>
          <h2>Daily limits</h2>
        </div>
      </div>

      <div className="limit-progress-group">
        <LimitProgress
          label="Daily amount"
          value={formatMoney(transferLimits.dailyTransferAmountUsed, transferLimits.currency)}
          limit={formatMoney(transferLimits.dailyTransferAmountLimit, transferLimits.currency)}
          remaining={`${formatMoney(transferLimits.dailyTransferAmountRemaining, transferLimits.currency)} remaining`}
          percent={amountUsage}
          isReached={transferLimits.amountLimitReached}
        />
        <LimitProgress
          label="Daily transfers"
          value={transferLimits.dailyTransferCountUsed}
          limit={transferLimits.dailyTransferCountLimit}
          remaining={`${transferLimits.dailyTransferCountRemaining} remaining`}
          percent={countUsage}
          isReached={transferLimits.countLimitReached}
        />
      </div>

      <div className="single-limit-note">
        <Gauge size={18} />
        <div>
          <span>Single transfer limit</span>
          <strong>{formatMoney(transferLimits.singleTransferLimit, transferLimits.currency)}</strong>
        </div>
      </div>

      <p className="transfer-limit-reset">
        Resets {formatDateTime(transferLimits.resetAt)}
      </p>
    </section>
  )
}

function LimitProgress({ label, value, limit, remaining, percent, isReached }) {
  return (
    <article className="limit-progress-card">
      <div className="limit-progress-meta">
        <span>{label}</span>
        <strong>{value} / {limit}</strong>
        <small className={isReached ? 'limit-reached' : ''}>{isReached ? 'Limit reached' : remaining}</small>
      </div>
      <div className="limit-progress-track" aria-hidden="true">
        <span style={{ width: `${percent}%` }} />
      </div>
    </article>
  )
}
