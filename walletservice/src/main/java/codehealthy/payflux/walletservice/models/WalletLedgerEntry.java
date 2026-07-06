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
@Table(name = "wallet_ledger_entries")
public class WalletLedgerEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "wallet_id", nullable = false)
	private Wallet wallet;

	@Column(nullable = false, length = 80)
	private String transactionReference;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private WalletLedgerEntryType entryType;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal balanceAfter;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(length = 240)
	private String description;

	@Column(nullable = false)
	private Instant createdAt;

	protected WalletLedgerEntry() {
	}

	public WalletLedgerEntry(
			Wallet wallet,
			String transactionReference,
			WalletLedgerEntryType entryType,
			BigDecimal amount,
			BigDecimal balanceAfter,
			String currency,
			String description
	) {
		this.wallet = wallet;
		this.transactionReference = transactionReference;
		this.entryType = entryType;
		this.amount = amount;
		this.balanceAfter = balanceAfter;
		this.currency = currency;
		this.description = description;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Wallet getWallet() {
		return wallet;
	}

	public String getTransactionReference() {
		return transactionReference;
	}

	public WalletLedgerEntryType getEntryType() {
		return entryType;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public BigDecimal getBalanceAfter() {
		return balanceAfter;
	}

	public String getCurrency() {
		return currency;
	}

	public String getDescription() {
		return description;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
