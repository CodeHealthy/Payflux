package codehealthy.payflux.authservice.services;

import codehealthy.payflux.audit.events.AuditTrailEvent;
import codehealthy.payflux.authservice.dto.AuthResponse;
import codehealthy.payflux.authservice.dto.ForgotPasswordRequest;
import codehealthy.payflux.authservice.dto.LoginRequest;
import codehealthy.payflux.authservice.dto.PasswordRecoveryQuestionResponse;
import codehealthy.payflux.authservice.dto.RefreshTokenRequest;
import codehealthy.payflux.authservice.dto.RegistrationResponse;
import codehealthy.payflux.authservice.dto.RegisterRequest;
import codehealthy.payflux.authservice.dto.ResetPasswordRequest;
import codehealthy.payflux.authservice.dto.UpdatePasswordRequest;
import codehealthy.payflux.authservice.dto.UpdateProfileRequest;
import codehealthy.payflux.authservice.dto.UpdateSecurityQuestionRequest;
import codehealthy.payflux.authservice.dto.UserResponse;
import codehealthy.payflux.authservice.dto.VerifyEmailRequest;
import codehealthy.payflux.authservice.events.UserRegisteredEvent;
import codehealthy.payflux.authservice.models.AppUser;
import codehealthy.payflux.authservice.models.UserRole;
import codehealthy.payflux.authservice.repositories.AppUserRepository;
import codehealthy.payflux.authservice.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final UserEventPublisher userEventPublisher;
	private final LoginProtectionService loginProtectionService;
	private final RefreshTokenService refreshTokenService;
	private final EmailVerificationService emailVerificationService;
	private final PasswordResetCodeService passwordResetCodeService;
	private final AuditEventPublisher auditEventPublisher;

	public AuthService(
			AppUserRepository appUserRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			UserEventPublisher userEventPublisher,
			LoginProtectionService loginProtectionService,
			RefreshTokenService refreshTokenService,
			EmailVerificationService emailVerificationService,
			PasswordResetCodeService passwordResetCodeService,
			AuditEventPublisher auditEventPublisher
	) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.userEventPublisher = userEventPublisher;
		this.loginProtectionService = loginProtectionService;
		this.refreshTokenService = refreshTokenService;
		this.emailVerificationService = emailVerificationService;
		this.passwordResetCodeService = passwordResetCodeService;
		this.auditEventPublisher = auditEventPublisher;
	}

	public RegistrationResponse register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();

		if (appUserRepository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
		}

		AppUser user = new AppUser(
				request.fullName().trim(),
				email,
				passwordEncoder.encode(request.password()),
				UserRole.USER,
				normalizeDisplayText(request.securityQuestion()),
				passwordEncoder.encode(normalizeSecurityAnswer(request.securityAnswer()))
		);
		AppUser savedUser = appUserRepository.save(user);
		emailVerificationService.sendInitialCode(savedUser);
		audit(
				"EMAIL_VERIFICATION_REQUESTED",
				savedUser.getId(),
				savedUser.getId(),
				"USER",
				savedUser.getId().toString(),
				"Email verification requested for " + savedUser.getEmail(),
				Map.of("email", savedUser.getEmail(), "reason", "registration")
		);

		return RegistrationResponse.verificationRequired(savedUser.getEmail());
	}

	public AuthResponse login(LoginRequest request, String clientIp) {
		String email = request.email().trim().toLowerCase();
		String normalizedClientIp = normalizeClientIp(clientIp);

		loginProtectionService.assertLoginAllowed(email, normalizedClientIp);
		AppUser user = appUserRepository.findByEmail(email)
				.orElseThrow(() -> {
					loginProtectionService.recordFailure(email, normalizedClientIp);
					audit(
							"LOGIN_FAILED",
							null,
							null,
							"USER",
							email,
							"Login failed for unknown email " + email,
							Map.of("email", email, "clientIp", normalizedClientIp, "reason", "unknown_user")
					);
					return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
				});

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			loginProtectionService.recordFailure(email, normalizedClientIp);
			audit(
					"LOGIN_FAILED",
					user.getId(),
					user.getId(),
					"USER",
					user.getId().toString(),
					"Login failed for " + user.getEmail(),
					Map.of("email", user.getEmail(), "clientIp", normalizedClientIp, "reason", "invalid_password")
			);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
		}
		if (!user.isEmailVerified()) {
			emailVerificationService.sendInitialCode(user);
			audit(
					"LOGIN_FAILED",
					user.getId(),
					user.getId(),
					"USER",
					user.getId().toString(),
					"Login blocked until email verification for " + user.getEmail(),
					Map.of("email", user.getEmail(), "clientIp", normalizedClientIp, "reason", "email_unverified")
			);
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email verification is required before login");
		}

		loginProtectionService.recordSuccess(email, normalizedClientIp);
		audit(
				"LOGIN_SUCCEEDED",
				user.getId(),
				user.getId(),
				"USER",
				user.getId().toString(),
				"User logged in successfully",
				Map.of("email", user.getEmail(), "clientIp", normalizedClientIp)
		);
		return createAuthResponse(user);
	}

	public void verifyEmail(VerifyEmailRequest request) {
		AppUser user = appUserRepository.findByEmail(normalizeEmail(request.email()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No PayFlux user exists for this email"));
		if (user.isEmailVerified()) {
			return;
		}

		emailVerificationService.verify(user, request.code());
		user.markEmailVerified();
		AppUser verifiedUser = appUserRepository.save(user);
		publishUserRegistered(verifiedUser);
		audit(
				"EMAIL_VERIFIED",
				verifiedUser.getId(),
				verifiedUser.getId(),
				"USER",
				verifiedUser.getId().toString(),
				"Email verified for " + verifiedUser.getEmail(),
				Map.of("email", verifiedUser.getEmail())
		);
	}

	public void resendEmailVerification(ForgotPasswordRequest request) {
		AppUser user = appUserRepository.findByEmail(normalizeEmail(request.email()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No PayFlux user exists for this email"));
		if (user.isEmailVerified()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already verified");
		}

		emailVerificationService.resendCode(user);
		audit(
				"EMAIL_VERIFICATION_REQUESTED",
				user.getId(),
				user.getId(),
				"USER",
				user.getId().toString(),
				"Email verification resent for " + user.getEmail(),
				Map.of("email", user.getEmail(), "reason", "resend")
		);
	}

	public AuthResponse refresh(RefreshTokenRequest request) {
		Long userId = refreshTokenService.consumeToken(request.refreshToken().trim())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired"));

		AppUser user = appUserRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token user is no longer available"));

		return createAuthResponse(user);
	}

	public void logout(RefreshTokenRequest request) {
		refreshTokenService.revokeToken(request.refreshToken().trim());
	}

	public PasswordRecoveryQuestionResponse recoveryQuestion(ForgotPasswordRequest request) {
		AppUser user = appUserRepository.findByEmail(normalizeEmail(request.email()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No PayFlux user exists for this email"));

		passwordResetCodeService.sendCode(user);
		audit(
				"PASSWORD_RESET_REQUESTED",
				user.getId(),
				user.getId(),
				"USER",
				user.getId().toString(),
				"Password reset requested for " + user.getEmail(),
				Map.of("email", user.getEmail(), "method", "email_code")
		);

		return new PasswordRecoveryQuestionResponse(user.getEmail(), user.getSecurityQuestion());
	}

	public void resetPassword(ResetPasswordRequest request) {
		AppUser user = appUserRepository.findByEmail(normalizeEmail(request.email()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No PayFlux user exists for this email"));
		String recoveryMethod;

		if (passwordResetCodeService.hasResetCode(request.resetCode())) {
			passwordResetCodeService.verify(user, request.resetCode());
			recoveryMethod = "email_code";
		} else {
			if (!user.hasSecurityQuestion()) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Password recovery is not configured for this account");
			}
			if (!passwordEncoder.matches(normalizeSecurityAnswer(request.securityAnswer()), user.getSecurityAnswerHash())) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Security answer is incorrect");
			}
			recoveryMethod = "security_question";
		}

		user.updatePassword(passwordEncoder.encode(request.newPassword()));
		appUserRepository.save(user);
		refreshTokenService.revokeAllForUser(user.getId());
		audit(
				"PASSWORD_RESET_COMPLETED",
				user.getId(),
				user.getId(),
				"USER",
				user.getId().toString(),
				"Password reset completed for " + user.getEmail(),
				Map.of("email", user.getEmail(), "method", recoveryMethod)
		);
	}

	public UserResponse me(Long userId) {
		return UserResponse.from(findUser(userId));
	}

	public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
		AppUser user = findUser(userId);
		user.updateProfile(normalizeDisplayText(request.fullName()));
		AppUser savedUser = appUserRepository.save(user);
		audit(
				"PROFILE_UPDATED",
				savedUser.getId(),
				savedUser.getId(),
				"USER",
				savedUser.getId().toString(),
				"User profile updated",
				Map.of("email", savedUser.getEmail())
		);
		return UserResponse.from(savedUser);
	}

	public void updatePassword(Long userId, UpdatePasswordRequest request) {
		AppUser user = findUser(userId);
		assertPasswordMatches(user, request.currentPassword());
		user.updatePassword(passwordEncoder.encode(request.newPassword()));
		appUserRepository.save(user);
		refreshTokenService.revokeAllForUser(user.getId());
		audit(
				"PASSWORD_CHANGED",
				user.getId(),
				user.getId(),
				"USER",
				user.getId().toString(),
				"User changed password from account settings",
				Map.of("email", user.getEmail())
		);
	}

	public UserResponse updateSecurityQuestion(Long userId, UpdateSecurityQuestionRequest request) {
		AppUser user = findUser(userId);
		assertPasswordMatches(user, request.currentPassword());
		user.updateSecurityQuestion(
				normalizeDisplayText(request.securityQuestion()),
				passwordEncoder.encode(normalizeSecurityAnswer(request.securityAnswer()))
		);
		AppUser savedUser = appUserRepository.save(user);
		audit(
				"SECURITY_QUESTION_UPDATED",
				savedUser.getId(),
				savedUser.getId(),
				"USER",
				savedUser.getId().toString(),
				"Security question updated",
				Map.of("email", savedUser.getEmail())
		);
		return UserResponse.from(savedUser);
	}

	private String normalizeClientIp(String clientIp) {
		if (clientIp == null || clientIp.isBlank()) {
			return "unknown";
		}

		return clientIp.trim();
	}

	private AppUser findUser(Long userId) {
		return appUserRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account is not available"));
	}

	private void assertPasswordMatches(AppUser user, String password) {
		if (!passwordEncoder.matches(password, user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
		}
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizeDisplayText(String value) {
		return value.trim().replaceAll("\\s+", " ");
	}

	private String normalizeSecurityAnswer(String value) {
		return normalizeDisplayText(value).toLowerCase(Locale.ROOT);
	}

	private AuthResponse createAuthResponse(AppUser user) {
		JwtService.GeneratedToken accessToken = jwtService.generateAccessToken(user);
		String refreshToken = refreshTokenService.issueToken(user.getId());
		return AuthResponse.bearer(accessToken.value(), refreshToken, accessToken.expiresAt(), UserResponse.from(user));
	}

	private void publishUserRegistered(AppUser user) {
		userEventPublisher.publishUserRegistered(new UserRegisteredEvent(
				user.getId(),
				user.getFullName(),
				user.getEmail(),
				user.getCreatedAt()
		));
	}

	private void audit(
			String action,
			Long actorUserId,
			Long subjectUserId,
			String aggregateType,
			String aggregateId,
			String summary,
			Map<String, Object> details
	) {
		auditEventPublisher.publish(new AuditTrailEvent(
				UUID.randomUUID().toString(),
				"authservice",
				action,
				actorUserId,
				subjectUserId,
				aggregateType,
				aggregateId,
				summary,
				details,
				Instant.now()
		));
	}
}
