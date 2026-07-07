package codehealthy.payflux.walletservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PendingTransfer(
		String confirmationId,
		Long ownerUserId,
		String email,
		String senderAccountNumber,
		String receiverAccountNumber,
		String receiverName,
		BigDecimal amount,
		String currency,
		String description,
		String idempotencyKey,
		String otp,
		Instant expiresAt
) {
}
