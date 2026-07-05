package codehealthy.payflux.accountservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "accounts")
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long ownerUserId;

	@Column(nullable = false, unique = true, length = 32)
	private String accountNumber;

	@Column(nullable = false)
	private String fullName;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private Instant createdAt;

	protected Account() {
	}

	public Account(Long ownerUserId, String accountNumber, String fullName, String email) {
		this.ownerUserId = ownerUserId;
		this.accountNumber = accountNumber;
		this.fullName = fullName;
		this.email = email;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Long getOwnerUserId() {
		return ownerUserId;
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

	public Instant getCreatedAt() {
		return createdAt;
	}
}
