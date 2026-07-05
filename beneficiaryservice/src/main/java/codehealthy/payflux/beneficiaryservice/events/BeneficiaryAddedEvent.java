package codehealthy.payflux.beneficiaryservice.events;

import java.time.Instant;

public record BeneficiaryAddedEvent(
		Long beneficiaryId,
		Long ownerUserId,
		String beneficiaryAccountNumber,
		String nickname,
		Instant createdAt
) {
}
