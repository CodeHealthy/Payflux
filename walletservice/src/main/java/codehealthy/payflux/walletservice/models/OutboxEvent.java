package codehealthy.payflux.walletservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 64)
	private String eventId;

	@Column(nullable = false, length = 120)
	private String topic;

	@Column(nullable = false, length = 160)
	private String eventType;

	@Column(nullable = false, length = 80)
	private String aggregateType;

	@Column(nullable = false, length = 80)
	private String aggregateId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private OutboxEventStatus status;

	@Column(nullable = false)
	private int attempts;

	@Column(length = 500)
	private String lastError;

	@Column(nullable = false)
	private Instant createdAt;

	private Instant publishedAt;

	protected OutboxEvent() {
	}

	public OutboxEvent(String eventId, String topic, String eventType, String aggregateType, String aggregateId, String payload) {
		this.eventId = eventId;
		this.topic = topic;
		this.eventType = eventType;
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.payload = payload;
		this.status = OutboxEventStatus.PENDING;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}

	public void markPublished() {
		this.status = OutboxEventStatus.PUBLISHED;
		this.publishedAt = Instant.now();
		this.lastError = null;
	}

	public void markFailed(String error) {
		this.status = OutboxEventStatus.FAILED;
		this.attempts++;
		this.lastError = error == null ? "Unknown publish failure" : error.substring(0, Math.min(error.length(), 500));
	}

	public void markPendingForRetry() {
		this.status = OutboxEventStatus.PENDING;
	}

	public Long getId() {
		return id;
	}

	public String getEventId() {
		return eventId;
	}

	public String getTopic() {
		return topic;
	}

	public String getEventType() {
		return eventType;
	}

	public String getPayload() {
		return payload;
	}

	public int getAttempts() {
		return attempts;
	}
}
