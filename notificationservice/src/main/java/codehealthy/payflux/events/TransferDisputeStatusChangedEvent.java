package codehealthy.payflux.events;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferDisputeStatusChangedEvent(
		String eventId,
		Long disputeId,
		Long ownerUserId,
		String transactionReference,
		String senderAccountNumber,
		String receiverAccountNumber,
		BigDecimal amount,
		String currency,
		String category,
		String status,
		String message,
		String resolutionNote,
		Instant occurredAt
) {
}
