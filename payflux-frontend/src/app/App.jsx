import { AuthPage } from '../features/auth/AuthPage'
import { BankingShell } from '../features/banking/BankingShell'
import { useBankingWorkspace } from '../features/banking/useBankingWorkspace'
import '../styles/AppLayout.css'

function App() {
  const { state, actions } = useBankingWorkspace()

  if (!state.currentUser) {
    return <AuthPage onAuthenticated={actions.handleAuthenticated} />
  }

  return <BankingShell state={state} actions={actions} />
}

export default App
