export function EmptyState({ imageSrc, isLoading, loadingText, text }) {
  return (
    <div className="empty-state">
      {imageSrc && !isLoading && <img src={imageSrc} alt="" />}
      <span>{isLoading ? loadingText : text}</span>
    </div>
  )
}
