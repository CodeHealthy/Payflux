import { EmptyState } from '../../components/EmptyState'
import { formatDateTime } from '../../utils/formatDateTime'

const noBeneficiariesImage = '/assets/empty-states/no-beneficiaries.png'

export function BeneficiariesTable({ beneficiaries, isLoading }) {
  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Saved recipients</p>
          <h2>Beneficiaries</h2>
        </div>
      </div>

      {beneficiaries.length === 0 ? (
        <EmptyState
          imageSrc={noBeneficiariesImage}
          isLoading={isLoading}
          loadingText="Loading beneficiaries..."
          text="No beneficiaries saved yet."
        />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Nickname</th>
                <th>Account number</th>
                <th>Name</th>
                <th>Status</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {beneficiaries.map((beneficiary) => (
                <tr key={beneficiary.id}>
                  <td>{beneficiary.nickname}</td>
                  <td className="mono-cell">{beneficiary.beneficiaryAccountNumber}</td>
                  <td>{beneficiary.beneficiaryName || 'Verified account'}</td>
                  <td>
                    <span className="status-pill">{beneficiary.status}</span>
                  </td>
                  <td>{formatDateTime(beneficiary.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
