package codehealthy.payflux.notificationservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long accountId;

	@Column(nullable = false)
	private Long ownerUserId;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String message;

	@Column(length = 64)
	private String sourceEventId;

	@Column(nullable = false)
	private Instant createdAt;

	@Column
	private Instant readAt;

	protected Notification() {
	}

	public Notification(Long accountId, Long ownerUserId, String email, String message) {
		this(accountId, ownerUserId, email, message, null);
	}

	public Notification(Long accountId, Long ownerUserId, String email, String message, String sourceEventId) {
		this.accountId = accountId;
		this.ownerUserId = ownerUserId;
		this.email = email;
		this.message = message;
		this.sourceEventId = sourceEventId;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Long getAccountId() {
		return accountId;
	}

	public Long getOwnerUserId() {
		return ownerUserId;
	}

	public String getEmail() {
		return email;
	}

	public String getMessage() {
		return message;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getReadAt() {
		return readAt;
	}

	public boolean isUnread() {
		return readAt == null;
	}

	public void markRead() {
		if (readAt == null) {
			readAt = Instant.now();
		}
	}
}
