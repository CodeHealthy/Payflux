export function FeedbackBanner({ error, successMessage, onDismiss }) {
  if (error) {
    return (
      <div className="alert alert-error" role="alert">
        <span>{error}</span>
        <button type="button" aria-label="Dismiss message" onClick={onDismiss}>
          Close
        </button>
      </div>
    )
  }

  if (successMessage) {
    return (
      <div className="alert alert-success" role="status">
        <span>{successMessage}</span>
        <button type="button" aria-label="Dismiss message" onClick={onDismiss}>
          Close
        </button>
      </div>
    )
  }

  return null
}
