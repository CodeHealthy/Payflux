package codehealthy.payflux.walletservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Service
public class TransferRateLimitService {

	private static final String KEY_PREFIX = "payflux:rate-limit:transfer-confirmation:";

	private final StringRedisTemplate redisTemplate;
	private final int maxRequests;
	private final Duration window;

	public TransferRateLimitService(
			StringRedisTemplate redisTemplate,
			@Value("${app.transfer.rate-limit.max-confirmations}") int maxRequests,
			@Value("${app.transfer.rate-limit.window-seconds}") long windowSeconds
	) {
		this.redisTemplate = redisTemplate;
		this.maxRequests = maxRequests;
		this.window = Duration.ofSeconds(windowSeconds);
	}

	public void assertAllowed(Long ownerUserId) {
		String key = KEY_PREFIX + ownerUserId;
		Long attempts = redisTemplate.opsForValue().increment(key);
		if (attempts != null && attempts == 1) {
			redisTemplate.expire(key, window);
		}

		if (attempts != null && attempts > maxRequests) {
			throw new ResponseStatusException(
					HttpStatus.TOO_MANY_REQUESTS,
					"Too many transfer confirmation requests. Please wait before trying again."
			);
		}
	}
}
