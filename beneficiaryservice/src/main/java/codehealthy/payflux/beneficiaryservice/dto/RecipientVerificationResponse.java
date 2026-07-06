package codehealthy.payflux.beneficiaryservice.dto;

public record RecipientVerificationResponse(
		String accountNumber,
		String displayName,
		String institutionName,
		boolean savedBeneficiary
) {
}
