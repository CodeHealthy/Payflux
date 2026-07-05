const actions = [
  { id: 'transfer', label: 'Send money', helper: 'Transfer to a PayFlux account' },
  { id: 'deposit', label: 'Add money', helper: 'Fund your wallet for testing' },
  { id: 'beneficiary', label: 'Add beneficiary', helper: 'Save a trusted recipient' },
]

export function QuickActions({ activeAction, onSelectAction }) {
  return (
    <section className="quick-actions" aria-label="Quick actions">
      {actions.map((action) => (
        <button
          className={activeAction === action.id ? 'quick-action active' : 'quick-action'}
          key={action.id}
          type="button"
          onClick={() => onSelectAction(action.id)}
        >
          <span>{action.label}</span>
          <small>{action.helper}</small>
        </button>
      ))}
    </section>
  )
}
