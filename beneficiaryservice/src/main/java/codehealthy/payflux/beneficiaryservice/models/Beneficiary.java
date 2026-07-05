package codehealthy.payflux.beneficiaryservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "beneficiaries")
public class Beneficiary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long ownerUserId;

	@Column(nullable = false, length = 32)
	private String beneficiaryAccountNumber;

	@Column(nullable = false, length = 100)
	private String nickname;

	private String beneficiaryName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private BeneficiaryStatus status;

	@Column(nullable = false)
	private Instant createdAt;

	protected Beneficiary() {
	}

	public Beneficiary(Long ownerUserId, String beneficiaryAccountNumber, String nickname, String beneficiaryName) {
		this.ownerUserId = ownerUserId;
		this.beneficiaryAccountNumber = beneficiaryAccountNumber;
		this.nickname = nickname;
		this.beneficiaryName = beneficiaryName;
		this.status = BeneficiaryStatus.ACTIVE;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Long getOwnerUserId() {
		return ownerUserId;
	}

	public String getBeneficiaryAccountNumber() {
		return beneficiaryAccountNumber;
	}

	public String getNickname() {
		return nickname;
	}

	public String getBeneficiaryName() {
		return beneficiaryName;
	}

	public BeneficiaryStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
