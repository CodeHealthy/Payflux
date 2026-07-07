package codehealthy.payflux.auditservice.dto;

import java.time.Instant;

public record AuditSummaryResponse(
		long totalRecords,
		long userRegistrations,
		long transfersCompleted,
		long beneficiariesAdded,
		Instant latestRecordAt
) {
}
