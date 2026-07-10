package codehealthy.payflux.walletservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "wallet_transfer_disputes")
public class WalletTransferDispute {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "transfer_id", nullable = false)
	private WalletTransfer transfer;

	@Column(nullable = false)
	private Long ownerUserId;

	@Column(nullable = false, length = 80)
	private String transactionReference;

	@Column(nullable = false, length = 80)
	private String category;

	@Column(nullable = false, length = 500)
	private String message;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private WalletTransferDisputeStatus status;

	@Column(length = 500)
	private String resolutionNote;

	private Long reviewedByUserId;

	private Instant reviewedAt;

	private Instant resolvedAt;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected WalletTransferDispute() {
	}

	public WalletTransferDispute(WalletTransfer transfer, Long ownerUserId, String category, String message) {
		this.transfer = transfer;
		this.ownerUserId = ownerUserId;
		this.transactionReference = transfer.getTransactionReference();
		this.category = category;
		this.message = message;
		this.status = WalletTransferDisputeStatus.OPEN;
	}

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = Instant.now();
	}

	public void markUnderReview(Long adminUserId) {
		this.status = WalletTransferDisputeStatus.UNDER_REVIEW;
		this.reviewedByUserId = adminUserId;
		this.reviewedAt = Instant.now();
	}

	public void resolve(Long adminUserId, String resolutionNote) {
		this.status = WalletTransferDisputeStatus.RESOLVED;
		this.reviewedByUserId = adminUserId;
		this.resolutionNote = resolutionNote;
		this.resolvedAt = Instant.now();
		if (this.reviewedAt == null) {
			this.reviewedAt = this.resolvedAt;
		}
	}

	public void reject(Long adminUserId, String resolutionNote) {
		this.status = WalletTransferDisputeStatus.REJECTED;
		this.reviewedByUserId = adminUserId;
		this.resolutionNote = resolutionNote;
		this.resolvedAt = Instant.now();
		if (this.reviewedAt == null) {
			this.reviewedAt = this.resolvedAt;
		}
	}

	public Long getId() {
		return id;
	}

	public WalletTransfer getTransfer() {
		return transfer;
	}

	public Long getOwnerUserId() {
		return ownerUserId;
	}

	public String getTransactionReference() {
		return transactionReference;
	}

	public String getCategory() {
		return category;
	}

	public String getMessage() {
		return message;
	}

	public WalletTransferDisputeStatus getStatus() {
		return status;
	}

	public String getResolutionNote() {
		return resolutionNote;
	}

	public Long getReviewedByUserId() {
		return reviewedByUserId;
	}

	public Instant getReviewedAt() {
		return reviewedAt;
	}

	public Instant getResolvedAt() {
		return resolvedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
