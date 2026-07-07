import { EmptyState } from '../components/EmptyState'
import { payfluxAssets } from '../assets/payfluxAssets'
import { formatDateTime } from '../utils/formatDateTime'

const emptyImage = payfluxAssets.admin.auditLog

export function AuditRecordsPage({ auditRecords, auditSummary, users, isLoading }) {
  return (
    <div className="admin-console">
      <section className="admin-hero panel">
        <div>
          <p className="eyebrow">Admin only</p>
          <h2>Operations control</h2>
          <p>
            Review customer access, monitor event activity, and inspect the latest
            compliance records across PayFlux services.
          </p>
        </div>

        <img src={payfluxAssets.admin.adminShield} alt="" />
      </section>

      <section className="admin-metrics" aria-label="Admin overview">
        <AdminMetric label="Total audit records" value={auditSummary?.totalRecords ?? 0} />
        <AdminMetric label="Registered users" value={users.length} />
        <AdminMetric label="Completed transfers" value={auditSummary?.transfersCompleted ?? 0} />
        <AdminMetric label="Beneficiaries added" value={auditSummary?.beneficiariesAdded ?? 0} />
        <AdminMetric
          label="Latest compliance event"
          value={auditSummary?.latestRecordAt ? formatDateTime(auditSummary.latestRecordAt) : 'None'}
        />
      </section>

      <section className="admin-grid">
        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Identity access</p>
              <h2>User directory</h2>
            </div>
          </div>

          {users.length === 0 ? (
            <EmptyState
              imageSrc={payfluxAssets.admin.compliance}
              isLoading={isLoading}
              loadingText="Loading users..."
              text="No users are available for review."
            />
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>User</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Created</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((user) => (
                    <tr key={user.id}>
                      <td>{user.fullName}</td>
                      <td>{user.email}</td>
                      <td><span className="status-pill">{user.role}</span></td>
                      <td>{formatDateTime(user.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Compliance trail</p>
              <h2>Recent audit records</h2>
            </div>
          </div>

          {auditRecords.length === 0 ? (
            <EmptyState
              imageSrc={emptyImage}
              isLoading={isLoading}
              loadingText="Loading audit records..."
              text="No audit records have been stored yet."
            />
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Action</th>
                    <th>Summary</th>
                    <th>Actor</th>
                    <th>Subject</th>
                    <th>Source</th>
                    <th>Created</th>
                  </tr>
                </thead>
                <tbody>
                  {auditRecords.map((record) => (
                    <tr key={record.id}>
                      <td><span className="status-pill">{record.action}</span></td>
                      <td>{record.summary}</td>
                      <td>{record.actorUserId || 'System'}</td>
                      <td>{record.subjectUserId || 'None'}</td>
                      <td>{record.sourceService}</td>
                      <td>{formatDateTime(record.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </section>
    </div>
  )
}

function AdminMetric({ label, value }) {
  return (
    <article className="stat-card">
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  )
}
