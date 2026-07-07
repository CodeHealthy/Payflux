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
          <h2>Profile and recovery settings</h2>
          <p>
            Keep your profile current, rotate your password, and maintain a recovery question
            for account access support.
          </p>
        </div>
        <div className="settings-status-card">
          <span>Recovery</span>
          <strong>{currentUser.securityQuestionConfigured ? 'Configured' : 'Not configured'}</strong>
        </div>
      </section>

      <section className="settings-grid">
        <form className="panel account-form" onSubmit={submitProfile}>
          <div>
            <p className="eyebrow">Profile</p>
            <h2>Personal details</h2>
          </div>
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
