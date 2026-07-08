import { ArrowRight, Landmark, LockKeyhole, ShieldCheck, WalletCards } from 'lucide-react'
import { payfluxAssets } from '../assets/payfluxAssets'
import '../styles/HomePage.css'

export function HomePage({ hasSession, onOpenAuth, onOpenDashboard }) {
  return (
    <main className="home-page">
      <header className="home-nav">
        <div className="brand-lockup compact">
          <img className="brand-logo" src={payfluxAssets.logos.icon} alt="" />
          <div>
            <strong>PayFlux</strong>
            <span>Banking OS</span>
          </div>
        </div>

        <nav aria-label="PayFlux access">
          {hasSession ? (
            <button className="primary-button" type="button" onClick={onOpenDashboard}>
              Dashboard
              <ArrowRight size={17} aria-hidden="true" />
            </button>
          ) : (
            <>
              <button className="ghost-button" type="button" onClick={() => onOpenAuth('login')}>
                Login
              </button>
              <button className="primary-button" type="button" onClick={() => onOpenAuth('register')}>
                Create account
                <ArrowRight size={17} aria-hidden="true" />
              </button>
            </>
          )}
        </nav>
      </header>

      <section className="home-hero">
        <div className="home-hero-copy">
          <p className="eyebrow">Secure digital banking</p>
          <h1>PayFlux Banking WebApp</h1>
          <p>
            A full-stack banking workspace for account onboarding, wallet transfers,
            beneficiaries, statements, admin review, Redis-backed OTP flows, and Kafka
            event-driven microservices.
          </p>
          <div className="home-actions">
            {hasSession ? (
              <button className="primary-button" type="button" onClick={onOpenDashboard}>
                Open dashboard
                <ArrowRight size={17} aria-hidden="true" />
              </button>
            ) : (
              <>
                <button className="primary-button" type="button" onClick={() => onOpenAuth('register')}>
                  Start with PayFlux
                  <ArrowRight size={17} aria-hidden="true" />
                </button>
                <button className="secondary-button" type="button" onClick={() => onOpenAuth('login')}>
                  Login to account
                </button>
              </>
            )}
          </div>
        </div>

        <div className="home-product-card" aria-label="PayFlux product preview">
          <span>Available balance</span>
          <strong>PKR 486,240.00</strong>
          <div>
            <small>Kafka events active</small>
            <small>JWT secured</small>
          </div>
        </div>
      </section>

      <section className="home-capabilities" aria-label="PayFlux capabilities">
        <Capability icon={<Landmark size={22} />} title="Account lifecycle" text="Verified signup, generated account numbers, wallet provisioning, and profile controls." />
        <Capability icon={<WalletCards size={22} />} title="Money movement" text="Beneficiaries, transfer confirmation OTPs, statements, ledger entries, and reversal review." />
        <Capability icon={<LockKeyhole size={22} />} title="Security controls" text="JWT auth, email verification, password recovery, Redis rate limits, and admin operations." />
        <Capability icon={<ShieldCheck size={22} />} title="Event-driven backend" text="Spring Boot microservices with Kafka topics, outbox publishing, PostgreSQL, Flyway, and Redis." />
      </section>
    </main>
  )
}

function Capability({ icon, title, text }) {
  return (
    <article>
      <span>{icon}</span>
      <strong>{title}</strong>
      <p>{text}</p>
    </article>
  )
}
