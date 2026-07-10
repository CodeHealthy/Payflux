import { cloneElement, useId } from 'react'

export function FormField({
  label,
  hint,
  icon: Icon,
  className = '',
  children,
}) {
  const generatedId = useId()
  const fieldId = children.props.id || children.props.name || generatedId
  const childClassName = ['field-input', Icon ? 'with-icon' : '', children.props.className]
    .filter(Boolean)
    .join(' ')

  return (
    <label className={['form-field', className].filter(Boolean).join(' ')} htmlFor={fieldId}>
      <span className="form-field-label">{label}</span>
      <span className="field-control">
        {Icon && <Icon className="field-icon" aria-hidden="true" size={18} />}
        {cloneElement(children, {
          id: fieldId,
          className: childClassName,
        })}
      </span>
      {hint && <span className="form-field-hint">{hint}</span>}
    </label>
  )
}
