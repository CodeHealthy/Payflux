package codehealthy.payflux.authservice.services;

import codehealthy.payflux.authservice.dto.AuthResponse;
import codehealthy.payflux.authservice.dto.LoginRequest;
import codehealthy.payflux.authservice.dto.RefreshTokenRequest;
import codehealthy.payflux.authservice.dto.RegisterRequest;
import codehealthy.payflux.authservice.dto.UserResponse;
import codehealthy.payflux.authservice.events.UserRegisteredEvent;
import codehealthy.payflux.authservice.models.AppUser;
import codehealthy.payflux.authservice.models.UserRole;
import codehealthy.payflux.authservice.repositories.AppUserRepository;
import codehealthy.payflux.authservice.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final UserEventPublisher userEventPublisher;
	private final LoginProtectionService loginProtectionService;
	private final RefreshTokenService refreshTokenService;

	public AuthService(
			AppUserRepository appUserRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			UserEventPublisher userEventPublisher,
			LoginProtectionService loginProtectionService,
			RefreshTokenService refreshTokenService
	) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.userEventPublisher = userEventPublisher;
		this.loginProtectionService = loginProtectionService;
		this.refreshTokenService = refreshTokenService;
	}

	public AuthResponse register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();

		if (appUserRepository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
		}

		AppUser user = new AppUser(
				request.fullName().trim(),
				email,
				passwordEncoder.encode(request.password()),
				UserRole.USER
		);
		AppUser savedUser = appUserRepository.save(user);

		userEventPublisher.publishUserRegistered(new UserRegisteredEvent(
				savedUser.getId(),
				savedUser.getFullName(),
				savedUser.getEmail(),
				savedUser.getCreatedAt()
		));

		return createAuthResponse(savedUser);
	}

	public AuthResponse login(LoginRequest request, String clientIp) {
		String email = request.email().trim().toLowerCase();
		String normalizedClientIp = normalizeClientIp(clientIp);

		loginProtectionService.assertLoginAllowed(email, normalizedClientIp);
		AppUser user = appUserRepository.findByEmail(email)
				.orElseThrow(() -> {
					loginProtectionService.recordFailure(email, normalizedClientIp);
					return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
				});

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			loginProtectionService.recordFailure(email, normalizedClientIp);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
		}

		loginProtectionService.recordSuccess(email, normalizedClientIp);
		return createAuthResponse(user);
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

	private String normalizeClientIp(String clientIp) {
		if (clientIp == null || clientIp.isBlank()) {
			return "unknown";
		}

		return clientIp.trim();
	}

	private AuthResponse createAuthResponse(AppUser user) {
		JwtService.GeneratedToken accessToken = jwtService.generateAccessToken(user);
		String refreshToken = refreshTokenService.issueToken(user.getId());
		return AuthResponse.bearer(accessToken.value(), refreshToken, accessToken.expiresAt(), UserResponse.from(user));
	}
}
