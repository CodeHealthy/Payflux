package codehealthy.payflux.auditservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "audit_records")
public class AuditRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 80)
	private String sourceService;

	@Column(nullable = false, length = 120)
	private String sourceEventId;

	@Column(nullable = false, length = 80)
	private String action;

	private Long actorUserId;

	private Long subjectUserId;

	@Column(nullable = false, length = 80)
	private String aggregateType;

	@Column(nullable = false, length = 120)
	private String aggregateId;

	@Column(nullable = false, length = 500)
	private String summary;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Column(nullable = false)
	private Instant eventOccurredAt;

	@Column(nullable = false)
	private Instant createdAt;

	protected AuditRecord() {
	}

	public AuditRecord(
			String sourceService,
			String sourceEventId,
			String action,
			Long actorUserId,
			Long subjectUserId,
			String aggregateType,
			String aggregateId,
			String summary,
			String payload,
			Instant eventOccurredAt
	) {
		this.sourceService = sourceService;
		this.sourceEventId = sourceEventId;
		this.action = action;
		this.actorUserId = actorUserId;
		this.subjectUserId = subjectUserId;
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.summary = summary;
		this.payload = payload;
		this.eventOccurredAt = eventOccurredAt;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getSourceService() {
		return sourceService;
	}

	public String getSourceEventId() {
		return sourceEventId;
	}

	public String getAction() {
		return action;
	}

	public Long getActorUserId() {
		return actorUserId;
	}

	public Long getSubjectUserId() {
		return subjectUserId;
	}

	public String getAggregateType() {
		return aggregateType;
	}

	public String getAggregateId() {
		return aggregateId;
	}

	public String getSummary() {
		return summary;
	}

	public String getPayload() {
		return payload;
	}

	public Instant getEventOccurredAt() {
		return eventOccurredAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
