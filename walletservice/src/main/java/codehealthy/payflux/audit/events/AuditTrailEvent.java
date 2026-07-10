package codehealthy.payflux.audit.events;

import java.time.Instant;
import java.util.Map;

public record AuditTrailEvent(
		String eventId,
		String sourceService,
		String action,
		Long actorUserId,
		Long subjectUserId,
		String aggregateType,
		String aggregateId,
		String summary,
		Map<String, Object> details,
		Instant occurredAt
) {
}
