package codehealthy.payflux.events;

import java.time.Instant;

public record AccountCreatedEvent(
		Long accountId,
		Long ownerUserId,
		String accountNumber,
		String fullName,
		String email,
		Instant createdAt
) {
}
