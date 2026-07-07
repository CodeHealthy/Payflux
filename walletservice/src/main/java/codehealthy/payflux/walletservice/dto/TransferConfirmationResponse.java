package codehealthy.payflux.walletservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferConfirmationResponse(
		String confirmationId,
		String receiverAccountNumber,
		String receiverName,
		BigDecimal amount,
		String currency,
		String description,
		Instant expiresAt,
		String idempotencyKey
) {
}
