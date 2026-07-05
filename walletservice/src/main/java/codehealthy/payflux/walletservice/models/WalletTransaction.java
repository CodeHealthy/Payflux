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
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "wallet_id", nullable = false)
	private Wallet wallet;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "counterparty_wallet_id")
	private Wallet counterpartyWallet;

	@Column(nullable = false, unique = true, length = 64)
	private String transactionReference;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private WalletTransactionType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private WalletTransactionStatus status;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(length = 240)
	private String description;

	@Column(length = 32)
	private String counterpartyAccountNumber;

	@Column(nullable = false)
	private Instant createdAt;

	protected WalletTransaction() {
	}

	public WalletTransaction(
			Wallet wallet,
			Wallet counterpartyWallet,
			String transactionReference,
			WalletTransactionType type,
			BigDecimal amount,
			String currency,
			String description,
			String counterpartyAccountNumber
	) {
		this.wallet = wallet;
		this.counterpartyWallet = counterpartyWallet;
		this.transactionReference = transactionReference;
		this.type = type;
		this.status = WalletTransactionStatus.COMPLETED;
		this.amount = amount;
		this.currency = currency;
		this.description = description;
		this.counterpartyAccountNumber = counterpartyAccountNumber;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getTransactionReference() {
		return transactionReference;
	}

	public WalletTransactionType getType() {
		return type;
	}

	public WalletTransactionStatus getStatus() {
		return status;
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

	public String getCounterpartyAccountNumber() {
		return counterpartyAccountNumber;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
