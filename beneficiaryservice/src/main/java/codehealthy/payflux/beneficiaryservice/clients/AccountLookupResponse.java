package codehealthy.payflux.beneficiaryservice.clients;

public record AccountLookupResponse(
		Long ownerUserId,
		String accountNumber,
		String fullName
) {
}
