import { BeneficiaryCreationPanel } from '../features/beneficiaries/BeneficiaryCreationPanel'
import { BeneficiariesTable } from '../features/beneficiaries/BeneficiariesTable'

export function BeneficiariesPage({
  beneficiaries,
  isLoading,
  isCreatingBeneficiary,
  onCreateBeneficiary,
}) {
  return (
    <section className="workspace-grid detail-section">
      <BeneficiaryCreationPanel
        isSubmitting={isCreatingBeneficiary}
        onCreateBeneficiary={onCreateBeneficiary}
      />
      <BeneficiariesTable beneficiaries={beneficiaries} isLoading={isLoading} />
    </section>
  )
}
