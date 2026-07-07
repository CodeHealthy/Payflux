package codehealthy.payflux.events;

import java.time.Instant;

public record AdminWalletStatusChangedEvent(
		String eventId,
		Long adminUserId,
		Long ownerUserId,
		String accountNumber,
		String previousStatus,
		String newStatus,
		String reason,
		Instant changedAt
) {
}
