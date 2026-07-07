package codehealthy.payflux.events;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferOtpRequestedEvent(
		String eventId,
		Long ownerUserId,
		String email,
		String receiverName,
		String receiverAccountNumber,
		BigDecimal amount,
		String currency,
		String otp,
		Instant expiresAt,
		Instant requestedAt
) {
}
