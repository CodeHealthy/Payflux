package codehealthy.payflux.authservice.services;

import codehealthy.payflux.authservice.dto.AuthResponse;
import codehealthy.payflux.authservice.dto.LoginRequest;
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

	public AuthService(
			AppUserRepository appUserRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			UserEventPublisher userEventPublisher
	) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.userEventPublisher = userEventPublisher;
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

	public AuthResponse login(LoginRequest request) {
		String email = request.email().trim().toLowerCase();
		AppUser user = appUserRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
		}

		return createAuthResponse(user);
	}

	private AuthResponse createAuthResponse(AppUser user) {
		JwtService.GeneratedToken accessToken = jwtService.generateAccessToken(user);
		return AuthResponse.bearer(accessToken.value(), accessToken.expiresAt(), UserResponse.from(user));
	}
}
