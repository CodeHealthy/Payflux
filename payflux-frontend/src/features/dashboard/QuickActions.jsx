import { Send, ShieldPlus, WalletCards } from 'lucide-react'

const actions = [
  { id: 'transfer', label: 'Send money', helper: 'Transfer to a PayFlux account', Icon: Send },
  { id: 'deposit', label: 'Add money', helper: 'Fund your wallet securely', Icon: WalletCards },
  { id: 'beneficiary', label: 'Add beneficiary', helper: 'Save a trusted recipient', Icon: ShieldPlus },
]

export function QuickActions({ activeAction, onSelectAction }) {
  return (
    <section className="quick-actions" aria-label="Quick actions">
      {actions.map((action) => {
        const Icon = action.Icon

        return (
          <button
            className={activeAction === action.id ? 'quick-action active' : 'quick-action'}
            key={action.id}
            type="button"
            onClick={() => onSelectAction(action.id)}
          >
            <span className="quick-action-icon">
              <Icon size={20} aria-hidden="true" />
            </span>
            <span>{action.label}</span>
            <small>{action.helper}</small>
          </button>
        )
      })}
    </section>
  )
}
