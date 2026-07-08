import { useState } from 'react'

export function SettingsPage({
  currentUser,
  isUpdatingSettings,
  onUpdateProfile,
  onUpdatePassword,
  onUpdateSecurityQuestion,
}) {
  const [profileForm, setProfileForm] = useState({ fullName: currentUser.fullName || '' })
  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '' })
  const [securityForm, setSecurityForm] = useState({
    currentPassword: '',
    securityQuestion: '',
    securityAnswer: '',
  })

  function handleProfileChange(event) {
    setProfileForm({ fullName: event.target.value })
  }

  function handlePasswordChange(event) {
    const { name, value } = event.target
    setPasswordForm((currentForm) => ({ ...currentForm, [name]: value }))
  }

  function handleSecurityChange(event) {
    const { name, value } = event.target
    setSecurityForm((currentForm) => ({ ...currentForm, [name]: value }))
  }

  async function submitProfile(event) {
    event.preventDefault()
    await onUpdateProfile(profileForm)
  }

  async function submitPassword(event) {
    event.preventDefault()
    const updated = await onUpdatePassword(passwordForm)
    if (updated) {
      setPasswordForm({ currentPassword: '', newPassword: '' })
    }
  }

  async function submitSecurityQuestion(event) {
    event.preventDefault()
    const updated = await onUpdateSecurityQuestion(securityForm)
    if (updated) {
      setSecurityForm({ currentPassword: '', securityQuestion: '', securityAnswer: '' })
    }
  }

  return (
    <div className="settings-page">
      <section className="panel settings-hero">
        <div>
          <p className="eyebrow">Account security</p>
          <h2>Account settings</h2>
          <p>
            Manage identity details, password access, recovery methods, and security posture
            for your PayFlux profile.
          </p>
        </div>
        <div className="settings-status-row">
          <div className="settings-status-card">
            <span>Email</span>
            <strong>{currentUser.emailVerified ? 'Verified' : 'Needs verification'}</strong>
          </div>
          <div className="settings-status-card">
            <span>Recovery</span>
            <strong>{currentUser.securityQuestionConfigured ? 'Configured' : 'Not configured'}</strong>
          </div>
        </div>
      </section>

      <section className="settings-overview-grid" aria-label="Security overview">
        <article className="settings-insight-card">
          <span>Primary recovery</span>
          <strong>Email reset code</strong>
          <p>Use a short-lived email code to reset your password securely.</p>
        </article>
        <article className="settings-insight-card">
          <span>Backup recovery</span>
          <strong>{currentUser.securityQuestionConfigured ? 'Security question active' : 'Setup needed'}</strong>
          <p>Keep a private recovery answer available for account support fallback.</p>
        </article>
        <article className="settings-insight-card">
          <span>Session safety</span>
          <strong>Password rotation</strong>
          <p>Changing your password signs this account out from existing sessions.</p>
        </article>
      </section>

      <section className="settings-grid">
        <form className="panel account-form" onSubmit={submitProfile}>
          <div>
            <p className="eyebrow">Profile</p>
            <h2>Identity details</h2>
          </div>
          <label>
            Email
            <input value={currentUser.email} disabled />
          </label>
          <label>
            Full name
            <input
              value={profileForm.fullName}
              onChange={handleProfileChange}
              placeholder="Your full name"
              required
            />
          </label>
          <button className="primary-button" type="submit" disabled={isUpdatingSettings}>
            Save profile
          </button>
        </form>

        <form className="panel account-form" onSubmit={submitPassword}>
          <div>
            <p className="eyebrow">Password</p>
            <h2>Change password</h2>
          </div>
          <p className="form-support-text">
            Use a strong password you do not use anywhere else. Updating it revokes active sessions.
          </p>
          <label>
            Current password
            <input
              name="currentPassword"
              type="password"
              value={passwordForm.currentPassword}
              onChange={handlePasswordChange}
              required
            />
          </label>
          <label>
            New password
            <input
              name="newPassword"
              type="password"
              value={passwordForm.newPassword}
              onChange={handlePasswordChange}
              minLength="8"
              required
            />
          </label>
          <button className="primary-button" type="submit" disabled={isUpdatingSettings}>
            Update password
          </button>
        </form>

        <form className="panel account-form" onSubmit={submitSecurityQuestion}>
          <div>
            <p className="eyebrow">Recovery</p>
            <h2>Security question</h2>
          </div>
          <p className="form-support-text">
            This remains available as a backup to the email reset code flow.
          </p>
          <label>
            Current password
            <input
              name="currentPassword"
              type="password"
              value={securityForm.currentPassword}
              onChange={handleSecurityChange}
              required
            />
          </label>
          <label>
            Security question
            <input
              name="securityQuestion"
              value={securityForm.securityQuestion}
              onChange={handleSecurityChange}
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
              value={securityForm.securityAnswer}
              onChange={handleSecurityChange}
              placeholder="Private recovery answer"
              minLength="3"
              maxLength="120"
              required
            />
          </label>
          <button className="primary-button" type="submit" disabled={isUpdatingSettings}>
            Save recovery question
          </button>
        </form>
      </section>
    </div>
  )
}
