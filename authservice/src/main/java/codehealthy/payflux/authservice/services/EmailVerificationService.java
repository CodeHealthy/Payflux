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
public class EmailVerificationService {

	private static final String CODE_PREFIX = "payflux:auth:email-verification:";
	private static final String COOLDOWN_PREFIX = "payflux:auth:email-verification-cooldown:";
	private static final String ATTEMPT_PREFIX = "payflux:auth:email-verification-attempts:";
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final StringRedisTemplate redisTemplate;
	private final AuthEmailService authEmailService;
	private final Duration codeTtl;
	private final Duration resendCooldown;
	private final int maxAttempts;

	public EmailVerificationService(
			StringRedisTemplate redisTemplate,
			AuthEmailService authEmailService,
			@Value("${app.auth.email-verification.ttl-seconds}") long ttlSeconds,
			@Value("${app.auth.email-verification.resend-cooldown-seconds}") long resendCooldownSeconds,
			@Value("${app.auth.email-verification.max-attempts}") int maxAttempts
	) {
		this.redisTemplate = redisTemplate;
		this.authEmailService = authEmailService;
		this.codeTtl = Duration.ofSeconds(ttlSeconds);
		this.resendCooldown = Duration.ofSeconds(resendCooldownSeconds);
		this.maxAttempts = maxAttempts;
	}

	public void sendInitialCode(AppUser user) {
		String code = createCode();
		redisTemplate.opsForValue().set(codeKey(user.getEmail()), code, codeTtl);
		redisTemplate.opsForValue().set(cooldownKey(user.getEmail()), "1", resendCooldown);
		authEmailService.sendVerificationCode(user.getEmail(), user.getFullName(), code, codeTtl.toMinutes());
	}

	public void resendCode(AppUser user) {
		if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey(user.getEmail())))) {
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Verification code was sent recently");
		}

		sendInitialCode(user);
	}

	public void verify(AppUser user, String code) {
		String normalizedCode = requireCode(code);
		String storedCode = redisTemplate.opsForValue().get(codeKey(user.getEmail()));
		if (storedCode == null) {
			throw new ResponseStatusException(HttpStatus.GONE, "Email verification code expired");
		}

		if (!storedCode.equals(normalizedCode)) {
			registerFailedAttempt(user.getEmail());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email verification code");
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
			throw new ResponseStatusException(HttpStatus.LOCKED, "Email verification locked after too many invalid codes");
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

	private String requireCode(String code) {
		if (code == null || code.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification code is required");
		}

		return code.trim();
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
