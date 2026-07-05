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
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallets")
public class Wallet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private Long ownerUserId;

	@Column(nullable = false, unique = true)
	private Long accountId;

	@Column(nullable = false, unique = true, length = 32)
	private String accountNumber;

	@Column(nullable = false, length = 160)
	private String fullName;

	@Column(nullable = false, length = 190)
	private String email;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal availableBalance;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private WalletStatus status;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	@Version
	private Long version;

	protected Wallet() {
	}

	public Wallet(Long ownerUserId, Long accountId, String accountNumber, String fullName, String email, String currency) {
		this.ownerUserId = ownerUserId;
		this.accountId = accountId;
		this.accountNumber = accountNumber;
		this.fullName = fullName;
		this.email = email;
		this.currency = currency;
		this.availableBalance = BigDecimal.ZERO.setScale(2);
		this.status = WalletStatus.ACTIVE;
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

	public void credit(BigDecimal amount) {
		this.availableBalance = this.availableBalance.add(amount);
	}

	public void debit(BigDecimal amount) {
		this.availableBalance = this.availableBalance.subtract(amount);
	}

	public Long getId() {
		return id;
	}

	public Long getOwnerUserId() {
		return ownerUserId;
	}

	public Long getAccountId() {
		return accountId;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getCurrency() {
		return currency;
	}

	public BigDecimal getAvailableBalance() {
		return availableBalance;
	}

	public WalletStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
