import { useState } from 'react'
import { loginUser, registerUser } from '../../api/authApi'
import { saveSession } from './authSession'

const emptyForm = {
  fullName: '',
  email: '',
  password: '',
}

export function AuthPage({ onAuthenticated }) {
  const [mode, setMode] = useState('login')
  const [formValues, setFormValues] = useState(emptyForm)
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const isRegisterMode = mode === 'register'

  function handleChange(event) {
    const { name, value } = event.target
    setFormValues((currentValues) => ({
      ...currentValues,
      [name]: value,
    }))
  }

  function switchMode(nextMode) {
    setMode(nextMode)
    setError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)

    const payload = isRegisterMode
      ? formValues
      : {
          email: formValues.email,
          password: formValues.password,
        }

    try {
      const authResponse = isRegisterMode
        ? await registerUser(payload)
        : await loginUser(payload)

      saveSession(authResponse)
      onAuthenticated(authResponse.user)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-visual" aria-label="PayFlux overview">
        <div className="brand-lockup">
          <img
            className="brand-logo on-dark"
            src="/assets/logos/payflux-icon-transparent.png"
            alt=""
          />
          <div>
            <strong>PayFlux</strong>
            <span>Digital banking workspace</span>
          </div>
        </div>

        <div className="bank-card-preview">
          <span>PayFlux current account</span>
          <strong>PKR 486,240.00</strong>
          <div className="card-meta">
            <small>Protected access</small>
            <small>Account ready</small>
          </div>
        </div>

        <div className="auth-metrics">
          <article>
            <span>Transfers</span>
            <strong>24/7</strong>
          </article>
          <article>
            <span>Wallet</span>
            <strong>PKR</strong>
          </article>
          <article>
            <span>Security</span>
            <strong>Active</strong>
          </article>
        </div>
      </section>

      <section className="auth-panel">
        <div className="auth-copy">
          <p className="eyebrow">Secure access</p>
          <h1>{isRegisterMode ? 'Create your PayFlux profile' : 'Welcome back'}</h1>
          <p>
            Sign in to access your wallet, saved beneficiaries, statements, and
            account notifications.
          </p>
        </div>

        <div className="auth-tabs" role="tablist" aria-label="Authentication mode">
          <button
            className={mode === 'login' ? 'active' : ''}
            type="button"
            onClick={() => switchMode('login')}
          >
            Login
          </button>
          <button
            className={mode === 'register' ? 'active' : ''}
            type="button"
            onClick={() => switchMode('register')}
          >
            Register
          </button>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <form className="auth-form" onSubmit={handleSubmit}>
          {isRegisterMode && (
            <label>
              Full name
              <input
                name="fullName"
                type="text"
                value={formValues.fullName}
                onChange={handleChange}
                placeholder="Ali Khan"
                required
              />
            </label>
          )}

          <label>
            Email
            <input
              name="email"
              type="email"
              value={formValues.email}
              onChange={handleChange}
              placeholder="ali@example.com"
              required
            />
          </label>

          <label>
            Password
            <input
              name="password"
              type="password"
              value={formValues.password}
              onChange={handleChange}
              placeholder="Minimum 8 characters"
              minLength={isRegisterMode ? 8 : undefined}
              required
            />
          </label>

          <button className="primary-button" type="submit" disabled={isSubmitting}>
            {isSubmitting
              ? 'Please wait...'
              : isRegisterMode
                ? 'Create profile'
                : 'Login'}
          </button>
        </form>
      </section>
    </main>
  )
}
