import { useMemo, useState } from 'react'

function toDateInputValue(date) {
  return date.toISOString().slice(0, 10)
}

export function StatementExportPanel({ isExporting, onExportStatement }) {
  const defaults = useMemo(() => {
    const to = new Date()
    const from = new Date()
    from.setDate(to.getDate() - 30)

    return {
      from: toDateInputValue(from),
      to: toDateInputValue(to),
    }
  }, [])

  const [formValues, setFormValues] = useState(defaults)

  function updateField(event) {
    const { name, value } = event.target
    setFormValues((currentValues) => ({
      ...currentValues,
      [name]: value,
    }))
  }

  function handleSubmit(event) {
    event.preventDefault()
    onExportStatement(formValues)
  }

  return (
    <section className="panel statement-export-panel">
      <div>
        <p className="eyebrow">Ledger export</p>
        <h2>Download statement</h2>
        <p>Export wallet ledger activity with signed amounts and running balances.</p>
      </div>

      <form className="statement-export-form" onSubmit={handleSubmit}>
        <label>
          From
          <input
            name="from"
            type="date"
            value={formValues.from}
            onChange={updateField}
            required
          />
        </label>

        <label>
          To
          <input
            name="to"
            type="date"
            value={formValues.to}
            onChange={updateField}
            required
          />
        </label>

        <button className="secondary-button" type="submit" disabled={isExporting}>
          {isExporting ? 'Preparing...' : 'Download CSV'}
        </button>
      </form>
    </section>
  )
}
