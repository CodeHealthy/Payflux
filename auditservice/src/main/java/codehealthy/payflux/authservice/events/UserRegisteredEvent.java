package codehealthy.payflux.authservice.events;

import java.time.Instant;

public record UserRegisteredEvent(
		Long userId,
		String fullName,
		String email,
		Instant registeredAt
) {
}
