package codehealthy.payflux.authservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Service
public class LoginProtectionService {

	private static final String EMAIL_ATTEMPT_PREFIX = "payflux:auth:login-attempts:email:";
	private static final String IP_ATTEMPT_PREFIX = "payflux:auth:login-attempts:ip:";
	private static final String EMAIL_LOCK_PREFIX = "payflux:auth:login-lockout:email:";
	private static final String IP_LOCK_PREFIX = "payflux:auth:login-lockout:ip:";

	private final StringRedisTemplate redisTemplate;
	private final int maxEmailAttempts;
	private final int maxIpAttempts;
	private final Duration attemptWindow;
	private final Duration lockoutWindow;

	public LoginProtectionService(
			StringRedisTemplate redisTemplate,
			@Value("${app.auth.login.max-email-attempts}") int maxEmailAttempts,
			@Value("${app.auth.login.max-ip-attempts}") int maxIpAttempts,
			@Value("${app.auth.login.window-seconds}") long windowSeconds,
			@Value("${app.auth.login.lockout-seconds}") long lockoutSeconds
	) {
		this.redisTemplate = redisTemplate;
		this.maxEmailAttempts = maxEmailAttempts;
		this.maxIpAttempts = maxIpAttempts;
		this.attemptWindow = Duration.ofSeconds(windowSeconds);
		this.lockoutWindow = Duration.ofSeconds(lockoutSeconds);
	}

	public void assertLoginAllowed(String email, String clientIp) {
		if (Boolean.TRUE.equals(redisTemplate.hasKey(emailLockKey(email)))
				|| Boolean.TRUE.equals(redisTemplate.hasKey(ipLockKey(clientIp)))) {
			throw new ResponseStatusException(
					HttpStatus.TOO_MANY_REQUESTS,
					"Too many failed login attempts. Please wait before trying again."
			);
		}
	}

	public void recordFailure(String email, String clientIp) {
		long emailAttempts = increment(emailAttemptKey(email));
		long ipAttempts = increment(ipAttemptKey(clientIp));

		if (emailAttempts >= maxEmailAttempts) {
			lock(emailLockKey(email));
		}

		if (ipAttempts >= maxIpAttempts) {
			lock(ipLockKey(clientIp));
		}
	}

	public void recordSuccess(String email, String clientIp) {
		redisTemplate.delete(emailAttemptKey(email));
		redisTemplate.delete(ipAttemptKey(clientIp));
		redisTemplate.delete(emailLockKey(email));
		redisTemplate.delete(ipLockKey(clientIp));
	}

	private long increment(String key) {
		Long attempts = redisTemplate.opsForValue().increment(key);
		if (attempts != null && attempts == 1L) {
			redisTemplate.expire(key, attemptWindow);
		}

		return attempts == null ? 0L : attempts;
	}

	private void lock(String key) {
		redisTemplate.opsForValue().set(key, "LOCKED", lockoutWindow);
	}

	private String emailAttemptKey(String email) {
		return EMAIL_ATTEMPT_PREFIX + email;
	}

	private String ipAttemptKey(String clientIp) {
		return IP_ATTEMPT_PREFIX + clientIp;
	}

	private String emailLockKey(String email) {
		return EMAIL_LOCK_PREFIX + email;
	}

	private String ipLockKey(String clientIp) {
		return IP_LOCK_PREFIX + clientIp;
	}
}
