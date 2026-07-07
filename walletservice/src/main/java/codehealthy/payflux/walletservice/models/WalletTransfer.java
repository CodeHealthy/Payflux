package codehealthy.payflux.walletservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallet_transfers")
public class WalletTransfer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long ownerUserId;

	@Column(nullable = false, length = 80, unique = true)
	private String transactionReference;

	@Column(nullable = false, length = 120)
	private String idempotencyKey;

	@Column(length = 64)
	private String requestHash;

	@Column(nullable = false, length = 32)
	private String senderAccountNumber;

	@Column(nullable = false, length = 32)
	private String receiverAccountNumber;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(length = 240)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private WalletTransferStatus status;

	@Column(length = 500)
	private String failureReason;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected WalletTransfer() {
	}

	public WalletTransfer(
			Long ownerUserId,
			String transactionReference,
			String idempotencyKey,
			String senderAccountNumber,
			String receiverAccountNumber,
			BigDecimal amount,
			String currency,
			String description,
			String requestHash
	) {
		this.ownerUserId = ownerUserId;
		this.transactionReference = transactionReference;
		this.idempotencyKey = idempotencyKey;
		this.requestHash = requestHash;
		this.senderAccountNumber = senderAccountNumber;
		this.receiverAccountNumber = receiverAccountNumber;
		this.amount = amount;
		this.currency = currency;
		this.description = description;
		this.status = WalletTransferStatus.PENDING_CONFIRMATION;
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

	public void markProcessing() {
		this.status = WalletTransferStatus.PROCESSING;
		this.failureReason = null;
	}

	public void markCompleted() {
		this.status = WalletTransferStatus.COMPLETED;
		this.failureReason = null;
	}

	public void markFailed(String failureReason) {
		this.status = WalletTransferStatus.FAILED;
		this.failureReason = failureReason;
	}

	public void markReversed(String reason) {
		this.status = WalletTransferStatus.REVERSED;
		this.failureReason = reason;
	}

	public boolean hasSameRequestHash(String requestHash) {
		return this.requestHash == null || this.requestHash.equals(requestHash);
	}

	public Long getId() {
		return id;
	}

	public Long getOwnerUserId() {
		return ownerUserId;
	}

	public String getTransactionReference() {
		return transactionReference;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public String getRequestHash() {
		return requestHash;
	}

	public String getSenderAccountNumber() {
		return senderAccountNumber;
	}

	public String getReceiverAccountNumber() {
		return receiverAccountNumber;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	public String getDescription() {
		return description;
	}

	public WalletTransferStatus getStatus() {
		return status;
	}

	public String getFailureReason() {
		return failureReason;
	}
}
