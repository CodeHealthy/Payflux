package codehealthy.payflux.authservice.services;

import codehealthy.payflux.authservice.models.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class PasswordResetCodeService {

	private static final String CODE_PREFIX = "payflux:auth:password-reset:";
	private static final String COOLDOWN_PREFIX = "payflux:auth:password-reset-cooldown:";
	private static final String ATTEMPT_PREFIX = "payflux:auth:password-reset-attempts:";
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final StringRedisTemplate redisTemplate;
	private final AuthEmailService authEmailService;
	private final Duration codeTtl;
	private final Duration cooldown;
	private final int maxAttempts;

	public PasswordResetCodeService(
			StringRedisTemplate redisTemplate,
			AuthEmailService authEmailService,
			@Value("${app.auth.password-reset.ttl-seconds}") long ttlSeconds,
			@Value("${app.auth.password-reset.resend-cooldown-seconds}") long cooldownSeconds,
			@Value("${app.auth.password-reset.max-attempts}") int maxAttempts
	) {
		this.redisTemplate = redisTemplate;
		this.authEmailService = authEmailService;
		this.codeTtl = Duration.ofSeconds(ttlSeconds);
		this.cooldown = Duration.ofSeconds(cooldownSeconds);
		this.maxAttempts = maxAttempts;
	}

	public void sendCode(AppUser user) {
		if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey(user.getEmail())))) {
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Password reset code was sent recently");
		}

		String code = createCode();
		redisTemplate.opsForValue().set(codeKey(user.getEmail()), code, codeTtl);
		redisTemplate.opsForValue().set(cooldownKey(user.getEmail()), "1", cooldown);
		authEmailService.sendPasswordResetCode(user.getEmail(), user.getFullName(), code, codeTtl.toMinutes());
	}

	public boolean hasResetCode(String resetCode) {
		return resetCode != null && !resetCode.isBlank();
	}

	public void verify(AppUser user, String resetCode) {
		String normalizedCode = requireCode(resetCode);
		String storedCode = redisTemplate.opsForValue().get(codeKey(user.getEmail()));
		if (storedCode == null) {
			throw new ResponseStatusException(HttpStatus.GONE, "Password reset code expired");
		}

		if (!storedCode.equals(normalizedCode)) {
			registerFailedAttempt(user.getEmail());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password reset code");
		}

		clear(user.getEmail());
	}

	private void registerFailedAttempt(String email) {
		String attemptKey = attemptKey(email);
		Long attempts = redisTemplate.opsForValue().increment(attemptKey);
		if (attempts != null && attempts == 1) {
			redisTemplate.expire(attemptKey, codeTtl);
		}

		if (attempts != null && attempts >= maxAttempts) {
			clear(email);
			throw new ResponseStatusException(HttpStatus.LOCKED, "Password reset locked after too many invalid codes");
		}
	}

	private void clear(String email) {
		redisTemplate.delete(codeKey(email));
		redisTemplate.delete(cooldownKey(email));
		redisTemplate.delete(attemptKey(email));
	}

	private String createCode() {
		return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
	}

	private String requireCode(String resetCode) {
		if (resetCode == null || resetCode.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset code is required");
		}

		return resetCode.trim();
	}

	private String codeKey(String email) {
		return CODE_PREFIX + email;
	}

	private String cooldownKey(String email) {
		return COOLDOWN_PREFIX + email;
	}

	private String attemptKey(String email) {
		return ATTEMPT_PREFIX + email;
	}
}
