import { useState } from 'react'

const initialForm = {
  beneficiaryAccountNumber: '',
  nickname: '',
}

export function BeneficiaryCreationPanel({ isSubmitting, onCreateBeneficiary }) {
  const [formValues, setFormValues] = useState(initialForm)

  function handleChange(event) {
    const { name, value } = event.target
    setFormValues((currentValues) => ({
      ...currentValues,
      [name]: value,
    }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    await onCreateBeneficiary(formValues)
    setFormValues(initialForm)
  }

  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Beneficiary service</p>
          <h2>Add beneficiary</h2>
        </div>
      </div>

      <form className="account-form" onSubmit={handleSubmit}>
        <label>
          Account number
          <input
            name="beneficiaryAccountNumber"
            type="text"
            inputMode="numeric"
            value={formValues.beneficiaryAccountNumber}
            onChange={handleChange}
            placeholder="920100000001"
            maxLength={32}
            required
          />
        </label>

        <label>
          Nickname
          <input
            name="nickname"
            type="text"
            value={formValues.nickname}
            onChange={handleChange}
            placeholder="Ali"
            maxLength={100}
            required
          />
        </label>

        <button className="primary-button" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Saving...' : 'Save beneficiary'}
        </button>
      </form>
    </section>
  )
}
