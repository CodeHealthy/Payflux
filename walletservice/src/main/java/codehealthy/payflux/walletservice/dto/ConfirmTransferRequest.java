package codehealthy.payflux.walletservice.dto;

public record ConfirmTransferRequest(
		String confirmationId,
		String idempotencyKey,
		String otp
) {
}
