package codehealthy.payflux.beneficiaryservice.dto;

import codehealthy.payflux.beneficiaryservice.models.Beneficiary;
import codehealthy.payflux.beneficiaryservice.models.BeneficiaryStatus;

import java.time.Instant;

public record BeneficiaryResponse(
		Long id,
		String beneficiaryAccountNumber,
		String nickname,
		String beneficiaryName,
		BeneficiaryStatus status,
		Instant createdAt
) {
	public static BeneficiaryResponse from(Beneficiary beneficiary) {
		return new BeneficiaryResponse(
				beneficiary.getId(),
				beneficiary.getBeneficiaryAccountNumber(),
				beneficiary.getNickname(),
				beneficiary.getBeneficiaryName(),
				beneficiary.getStatus(),
				beneficiary.getCreatedAt()
		);
	}
}
