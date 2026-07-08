package codehealthy.payflux.walletservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

@Service
public class TransferOtpResendLimitService {

	private static final String COOLDOWN_KEY_PREFIX = "payflux:transfer-otp-resend-cooldown:";
	private static final String COUNT_KEY_PREFIX = "payflux:transfer-otp-resend-count:";

	private final StringRedisTemplate redisTemplate;
	private final Duration cooldown;
	private final Duration window;
	private final int maxResends;

	public TransferOtpResendLimitService(
			StringRedisTemplate redisTemplate,
			@Value("${app.transfer.otp-resend.cooldown-seconds}") long cooldownSeconds,
			@Value("${app.transfer.otp-resend.window-seconds}") long windowSeconds,
			@Value("${app.transfer.otp-resend.max-resends}") int maxResends
	) {
		this.redisTemplate = redisTemplate;
		this.cooldown = Duration.ofSeconds(cooldownSeconds);
		this.window = Duration.ofSeconds(windowSeconds);
		this.maxResends = maxResends;
	}

	public Instant startInitialCooldown(Long ownerUserId, String confirmationId) {
		redisTemplate.opsForValue().set(cooldownKey(ownerUserId, confirmationId), "1", cooldown);
		return Instant.now().plus(cooldown);
	}

	public Instant assertResendAllowed(Long ownerUserId, String confirmationId) {
		String cooldownKey = cooldownKey(ownerUserId, confirmationId);
		Boolean cooldownStarted = redisTemplate.opsForValue().setIfAbsent(cooldownKey, "1", cooldown);
		if (Boolean.FALSE.equals(cooldownStarted)) {
			throw new ResponseStatusException(
					HttpStatus.TOO_MANY_REQUESTS,
					"Transfer confirmation code was sent recently. Please wait before requesting another code."
			);
		}

		String countKey = countKey(ownerUserId, confirmationId);
		Long resends = redisTemplate.opsForValue().increment(countKey);
		if (resends != null && resends == 1) {
			redisTemplate.expire(countKey, window);
		}

		if (resends != null && resends > maxResends) {
			throw new ResponseStatusException(
					HttpStatus.TOO_MANY_REQUESTS,
					"Too many transfer confirmation code requests. Start a new transfer request later."
			);
		}

		return Instant.now().plus(cooldown);
	}

	public void clear(Long ownerUserId, String confirmationId) {
		redisTemplate.delete(cooldownKey(ownerUserId, confirmationId));
		redisTemplate.delete(countKey(ownerUserId, confirmationId));
	}

	private String cooldownKey(Long ownerUserId, String confirmationId) {
		return COOLDOWN_KEY_PREFIX + ownerUserId + ":" + confirmationId;
	}

	private String countKey(Long ownerUserId, String confirmationId) {
		return COUNT_KEY_PREFIX + ownerUserId + ":" + confirmationId;
	}
}
