import { useState } from 'react'
import { ArrowLeft } from 'lucide-react'
import {
  loginUser,
  registerUser,
  resendEmailVerification,
  requestPasswordRecovery,
  resetPassword,
  verifyEmail,
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
  verificationCode: '',
  resetCode: '',
}

export function AuthPage({ initialMode = 'login', onAuthenticated, onBackHome }) {
  const [mode, setMode] = useState(initialMode)
  const [formValues, setFormValues] = useState(emptyForm)
  const [recoveryQuestion, setRecoveryQuestion] = useState(null)
  const [recoveryMethod, setRecoveryMethod] = useState('email')
  const [successMessage, setSuccessMessage] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const isRegisterMode = mode === 'register'
  const isRecoveryMode = mode === 'recovery'
  const isVerifyMode = mode === 'verify-email'

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
    setRecoveryMethod('email')
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

    if (isVerifyMode) {
      await handleVerificationSubmit()
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
      if (isRegisterMode) {
        const registrationResponse = await registerUser(payload)
        setSuccessMessage(registrationResponse.message || 'Profile created. Check your email for a verification code.')
        setFormValues((currentValues) => ({
          ...currentValues,
          password: '',
          verificationCode: '',
        }))
        setMode('verify-email')
        return
      }

      const authResponse = await loginUser(payload)

      saveSession(authResponse)
      onAuthenticated(authResponse.user)
    } catch (requestError) {
      if (requestError.code === 'EMAIL_VERIFICATION_REQUIRED') {
        setSuccessMessage('Verify your email before signing in. We sent a fresh code to your inbox.')
        setMode('verify-email')
        return
      }

      setError(requestError.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleVerificationSubmit() {
    try {
      await verifyEmail({
        email: formValues.email,
        code: formValues.verificationCode,
      })
      setSuccessMessage('Email verified. You can now sign in.')
      setFormValues((currentValues) => ({
        ...emptyForm,
        email: currentValues.email,
      }))
      setMode('login')
    } catch (requestError) {
      setError(verificationErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleResendVerification() {
    setError('')
    setSuccessMessage('')
    setIsSubmitting(true)

    try {
      await resendEmailVerification({ email: formValues.email })
      setSuccessMessage('A new verification code has been sent to your email.')
    } catch (requestError) {
      setError(verificationErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleRecoverySubmit() {
    try {
      if (!recoveryQuestion) {
        const response = await requestPasswordRecovery({ email: formValues.email })
        setRecoveryQuestion(response)
        setRecoveryMethod('email')
        setSuccessMessage('We sent a password reset code to your email.')
        return
      }

      await resetPassword({
        email: formValues.email,
        resetCode: recoveryMethod === 'email' ? formValues.resetCode : undefined,
        securityAnswer: recoveryMethod === 'question' ? formValues.securityAnswer : undefined,
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
      setError(passwordResetErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleResendPasswordReset() {
    setError('')
    setSuccessMessage('')
    setIsSubmitting(true)

    try {
      await requestPasswordRecovery({ email: formValues.email })
      setSuccessMessage('A new password reset code has been sent to your email.')
    } catch (requestError) {
      setError(passwordResetErrorMessage(requestError))
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
        {onBackHome && (
          <button className="text-button auth-back-button" type="button" onClick={onBackHome}>
            <ArrowLeft size={16} aria-hidden="true" />
            PayFlux home
          </button>
        )}

        <div className="auth-copy">
          <p className="eyebrow">Secure access</p>
          <h1>
            {isRegisterMode && 'Create your PayFlux profile'}
            {mode === 'login' && 'Welcome back'}
            {isRecoveryMode && 'Recover your account'}
            {isVerifyMode && 'Verify your email'}
          </h1>
          <p>
            Sign in to access your wallet, saved beneficiaries, statements, and
            account notifications.
          </p>
        </div>

        {!isVerifyMode && (
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
        )}

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

          {!isVerifyMode && (
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
          )}

          {isVerifyMode && (
            <>
              <div className="recovery-question-card">
                <span>Verification email</span>
                <strong>{formValues.email}</strong>
              </div>

              <label>
                Verification code
                <input
                  inputMode="numeric"
                  maxLength="6"
                  name="verificationCode"
                  pattern="[0-9]{6}"
                  value={formValues.verificationCode}
                  onChange={(event) => setFormValues((currentValues) => ({
                    ...currentValues,
                    verificationCode: event.target.value.replace(/\D/g, '').slice(0, 6),
                  }))}
                  placeholder="000000"
                  required
                />
              </label>
            </>
          )}

          {!isRecoveryMode && !isVerifyMode && (
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
              <div className="recovery-method-tabs" role="tablist" aria-label="Password recovery method">
                <button
                  className={recoveryMethod === 'email' ? 'active' : ''}
                  type="button"
                  onClick={() => setRecoveryMethod('email')}
                >
                  Email code
                </button>
                {recoveryQuestion.securityQuestion && (
                  <button
                    className={recoveryMethod === 'question' ? 'active' : ''}
                    type="button"
                    onClick={() => setRecoveryMethod('question')}
                  >
                    Security answer
                  </button>
                )}
              </div>

              {recoveryMethod === 'email' && (
                <>
                  <div className="recovery-question-card">
                    <span>Reset email</span>
                    <strong>{recoveryQuestion.email}</strong>
                  </div>

                  <label>
                    Reset code
                    <input
                      inputMode="numeric"
                      maxLength="6"
                      name="resetCode"
                      pattern="[0-9]{6}"
                      value={formValues.resetCode}
                      onChange={(event) => setFormValues((currentValues) => ({
                        ...currentValues,
                        resetCode: event.target.value.replace(/\D/g, '').slice(0, 6),
                      }))}
                      placeholder="000000"
                      required
                    />
                  </label>
                </>
              )}

              {recoveryMethod === 'question' && recoveryQuestion.securityQuestion && (
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
                </>
              )}

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
            <>
              {recoveryQuestion && recoveryMethod === 'email' && (
                <button className="text-button" type="button" disabled={isSubmitting} onClick={handleResendPasswordReset}>
                  Resend reset code
                </button>
              )}
              <button className="text-button" type="button" onClick={() => switchMode('login')}>
                Back to login
              </button>
            </>
          )}

          {isVerifyMode && (
            <>
              <button className="text-button" type="button" disabled={isSubmitting} onClick={handleResendVerification}>
                Resend verification code
              </button>
              <button className="text-button" type="button" onClick={() => switchMode('login')}>
                Back to login
              </button>
            </>
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
  if (mode === 'verify-email') {
    return 'Verify email'
  }

  return 'Login'
}

function verificationErrorMessage(error) {
  if (error.code === 'INVALID_EMAIL_VERIFICATION_CODE') {
    return 'The verification code is incorrect. Check your email and try again.'
  }
  if (error.code === 'EMAIL_VERIFICATION_EXPIRED') {
    return 'This verification code has expired. Request a new code and try again.'
  }
  if (error.code === 'EMAIL_VERIFICATION_RESEND_COOLDOWN') {
    return 'A verification code was sent recently. Please wait before requesting another code.'
  }
  if (error.code === 'EMAIL_VERIFICATION_LOCKED') {
    return 'Too many incorrect verification attempts. Request a new code later.'
  }

  return error.message
}

function passwordResetErrorMessage(error) {
  if (error.code === 'INVALID_PASSWORD_RESET_CODE') {
    return 'The password reset code is incorrect. Check your email and try again.'
  }
  if (error.code === 'PASSWORD_RESET_CODE_EXPIRED') {
    return 'This password reset code has expired. Request a new code and try again.'
  }
  if (error.code === 'PASSWORD_RESET_RESEND_COOLDOWN') {
    return 'A reset code was sent recently. Please wait before requesting another code.'
  }
  if (error.code === 'PASSWORD_RESET_LOCKED') {
    return 'Too many incorrect reset attempts. Request a new code later.'
  }
  if (error.code === 'SECURITY_ANSWER_INCORRECT') {
    return 'The security answer is incorrect. Try again or use the email reset code.'
  }

  return error.message
}
