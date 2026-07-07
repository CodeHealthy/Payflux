package codehealthy.payflux.authservice.models;

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

import java.time.Instant;

@Entity
@Table(name = "users")
public class AppUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String fullName;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String passwordHash;

	@Column(length = 160)
	private String securityQuestion;

	@Column(length = 255)
	private String securityAnswerHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserRole role = UserRole.USER;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected AppUser() {
	}

	public AppUser(
			String fullName,
			String email,
			String passwordHash,
			UserRole role,
			String securityQuestion,
			String securityAnswerHash
	) {
		this.fullName = fullName;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
		this.securityQuestion = securityQuestion;
		this.securityAnswerHash = securityAnswerHash;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}

	public void updateProfile(String fullName) {
		this.fullName = fullName;
	}

	public void updatePassword(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public void updateSecurityQuestion(String securityQuestion, String securityAnswerHash) {
		this.securityQuestion = securityQuestion;
		this.securityAnswerHash = securityAnswerHash;
	}

	public Long getId() {
		return id;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getSecurityQuestion() {
		return securityQuestion;
	}

	public String getSecurityAnswerHash() {
		return securityAnswerHash;
	}

	public boolean hasSecurityQuestion() {
		return securityQuestion != null && !securityQuestion.isBlank()
				&& securityAnswerHash != null && !securityAnswerHash.isBlank();
	}

	public UserRole getRole() {
		return role;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
