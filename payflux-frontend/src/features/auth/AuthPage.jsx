import { useState } from 'react'
import {
  loginUser,
  registerUser,
  requestPasswordRecovery,
  resetPassword,
} from '../../api/authApi'
import { payfluxAssets } from '../../assets/payfluxAssets'
import { saveSession } from './authSession'
import '../../styles/AuthPage.css'

const emptyForm = {
  fullName: '',
  email: '',
  password: '',
  securityQuestion: '',
  securityAnswer: '',
  newPassword: '',
}

export function AuthPage({ onAuthenticated }) {
  const [mode, setMode] = useState('login')
  const [formValues, setFormValues] = useState(emptyForm)
  const [recoveryQuestion, setRecoveryQuestion] = useState(null)
  const [successMessage, setSuccessMessage] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const isRegisterMode = mode === 'register'
  const isRecoveryMode = mode === 'recovery'

  function handleChange(event) {
    const { name, value } = event.target
    setFormValues((currentValues) => ({
      ...currentValues,
      [name]: value,
    }))
  }

  function switchMode(nextMode) {
    setMode(nextMode)
    setRecoveryQuestion(null)
    setSuccessMessage('')
    setError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setSuccessMessage('')
    setIsSubmitting(true)

    if (isRecoveryMode) {
      await handleRecoverySubmit()
      return
    }

    const payload = isRegisterMode
      ? {
          fullName: formValues.fullName,
          email: formValues.email,
          password: formValues.password,
          securityQuestion: formValues.securityQuestion,
          securityAnswer: formValues.securityAnswer,
        }
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

  async function handleRecoverySubmit() {
    try {
      if (!recoveryQuestion) {
        const response = await requestPasswordRecovery({ email: formValues.email })
        setRecoveryQuestion(response)
        return
      }

      await resetPassword({
        email: formValues.email,
        securityAnswer: formValues.securityAnswer,
        newPassword: formValues.newPassword,
      })
      setSuccessMessage('Password reset completed. You can sign in with your new password.')
      setRecoveryQuestion(null)
      setFormValues((currentValues) => ({
        ...emptyForm,
        email: currentValues.email,
      }))
      setMode('login')
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
            src={payfluxAssets.logos.icon}
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
          <h1>
            {isRegisterMode && 'Create your PayFlux profile'}
            {mode === 'login' && 'Welcome back'}
            {isRecoveryMode && 'Recover your account'}
          </h1>
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

        {successMessage && <div className="alert alert-success">{successMessage}</div>}
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

          {!isRecoveryMode && (
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
          )}

          {isRegisterMode && (
            <>
              <label>
                Security question
                <input
                  name="securityQuestion"
                  type="text"
                  value={formValues.securityQuestion}
                  onChange={handleChange}
                  placeholder="What was your first school called?"
                  minLength="12"
                  maxLength="160"
                  required
                />
              </label>

              <label>
                Security answer
                <input
                  name="securityAnswer"
                  type="text"
                  value={formValues.securityAnswer}
                  onChange={handleChange}
                  placeholder="Answer used for account recovery"
                  minLength="3"
                  maxLength="120"
                  required
                />
              </label>
            </>
          )}

          {isRecoveryMode && recoveryQuestion && (
            <>
              <div className="recovery-question-card">
                <span>Security question</span>
                <strong>{recoveryQuestion.securityQuestion}</strong>
              </div>

              <label>
                Security answer
                <input
                  name="securityAnswer"
                  type="text"
                  value={formValues.securityAnswer}
                  onChange={handleChange}
                  placeholder="Enter your answer"
                  minLength="3"
                  maxLength="120"
                  required
                />
              </label>

              <label>
                New password
                <input
                  name="newPassword"
                  type="password"
                  value={formValues.newPassword}
                  onChange={handleChange}
                  placeholder="Minimum 8 characters"
                  minLength="8"
                  required
                />
              </label>
            </>
          )}

          <button className="primary-button" type="submit" disabled={isSubmitting}>
            {isSubmitting
              ? 'Please wait...'
              : buttonText(mode, recoveryQuestion)}
          </button>

          {mode === 'login' && (
            <button className="text-button" type="button" onClick={() => switchMode('recovery')}>
              Forgot password?
            </button>
          )}

          {isRecoveryMode && (
            <button className="text-button" type="button" onClick={() => switchMode('login')}>
              Back to login
            </button>
          )}
        </form>
      </section>
    </main>
  )
}

function buttonText(mode, recoveryQuestion) {
  if (mode === 'register') {
    return 'Create profile'
  }
  if (mode === 'recovery') {
    return recoveryQuestion ? 'Reset password' : 'Continue'
  }

  return 'Login'
}
