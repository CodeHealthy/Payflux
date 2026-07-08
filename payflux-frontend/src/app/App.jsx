import { useState } from 'react'
import { AuthPage } from '../features/auth/AuthPage'
import { BankingShell } from '../features/banking/BankingShell'
import { useBankingWorkspace } from '../features/banking/useBankingWorkspace'
import { HomePage } from '../pages/HomePage'
import '../styles/AppLayout.css'

function App() {
  const { state, actions } = useBankingWorkspace()
  const [publicView, setPublicView] = useState('home')
  const [authMode, setAuthMode] = useState('login')

  function openAuth(mode) {
    setAuthMode(mode)
    setPublicView('auth')
  }

  const shellActions = {
    ...actions,
    handleLogout: async () => {
      await actions.handleLogout()
      setAuthMode('login')
      setPublicView('home')
    },
  }

  if (!state.currentUser && publicView === 'auth') {
    return (
      <AuthPage
        initialMode={authMode}
        onBackHome={() => setPublicView('home')}
        onAuthenticated={actions.handleAuthenticated}
      />
    )
  }

  if (!state.currentUser) {
    return (
      <HomePage
        hasSession={false}
        onOpenAuth={openAuth}
        onOpenDashboard={() => setPublicView('auth')}
      />
    )
  }

  return <BankingShell state={state} actions={shellActions} />
}

export default App
