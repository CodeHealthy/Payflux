package codehealthy.payflux.auditservice.dto;

import codehealthy.payflux.auditservice.models.AuditRecord;

import java.time.Instant;

public record AuditRecordResponse(
		Long id,
		String sourceService,
		String sourceEventId,
		String action,
		Long actorUserId,
		Long subjectUserId,
		String aggregateType,
		String aggregateId,
		String summary,
		String payload,
		Instant eventOccurredAt,
		Instant createdAt
) {
	public static AuditRecordResponse from(AuditRecord auditRecord) {
		return new AuditRecordResponse(
				auditRecord.getId(),
				auditRecord.getSourceService(),
				auditRecord.getSourceEventId(),
				auditRecord.getAction(),
				auditRecord.getActorUserId(),
				auditRecord.getSubjectUserId(),
				auditRecord.getAggregateType(),
				auditRecord.getAggregateId(),
				auditRecord.getSummary(),
				auditRecord.getPayload(),
				auditRecord.getEventOccurredAt(),
				auditRecord.getCreatedAt()
		);
	}
}
