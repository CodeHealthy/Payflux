import { EmptyState } from '../components/EmptyState'
import { payfluxAssets } from '../assets/payfluxAssets'
import { formatDateTime } from '../utils/formatDateTime'

const emptyImage = payfluxAssets.admin.auditLog

export function AuditRecordsPage({ auditRecords, isLoading }) {
  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Admin only</p>
          <h2>Audit records</h2>
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
  )
}
