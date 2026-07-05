package codehealthy.payflux.events;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferCompletedEvent(
		String eventId,
		String transactionReference,
		Long senderUserId,
		Long receiverUserId,
		String senderAccountNumber,
		String receiverAccountNumber,
		BigDecimal amount,
		String currency,
		String description,
		Instant completedAt
) {
}
