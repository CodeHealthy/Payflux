package codehealthy.payflux.walletservice.services;

import codehealthy.payflux.walletservice.dto.WalletDashboardResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Optional;

@Service
public class TransferIdempotencyService {

	private static final String KEY_PREFIX = "payflux:idempotency:wallet-transfer:";
	private static final String PROCESSING_VALUE = "PROCESSING";

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private final Duration ttl;

	public TransferIdempotencyService(
			StringRedisTemplate redisTemplate,
			ObjectMapper objectMapper,
			@Value("${app.transfer.idempotency-ttl-seconds}") long ttlSeconds
	) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
		this.ttl = Duration.ofSeconds(ttlSeconds);
	}

	public Optional<WalletDashboardResponse> findCompleted(Long ownerUserId, String idempotencyKey) {
		String payload = redisTemplate.opsForValue().get(key(ownerUserId, idempotencyKey));
		if (payload == null || PROCESSING_VALUE.equals(payload)) {
			return Optional.empty();
		}

		try {
			return Optional.of(objectMapper.readValue(payload, WalletDashboardResponse.class));
		} catch (JsonProcessingException ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read transfer result");
		}
	}

	public void claim(Long ownerUserId, String idempotencyKey) {
		Boolean claimed = redisTemplate.opsForValue().setIfAbsent(
				key(ownerUserId, idempotencyKey),
				PROCESSING_VALUE,
				ttl
		);

		if (!Boolean.TRUE.equals(claimed)) {
			throw new ResponseStatusException(
					HttpStatus.CONFLICT,
					"Transfer is already being processed. Please refresh shortly."
			);
		}
	}

	public void complete(Long ownerUserId, String idempotencyKey, WalletDashboardResponse response) {
		try {
			redisTemplate.opsForValue().set(key(ownerUserId, idempotencyKey), objectMapper.writeValueAsString(response), ttl);
		} catch (JsonProcessingException ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store transfer result");
		}
	}

	public void release(Long ownerUserId, String idempotencyKey) {
		redisTemplate.delete(key(ownerUserId, idempotencyKey));
	}

	private String key(Long ownerUserId, String idempotencyKey) {
		return KEY_PREFIX + ownerUserId + ":" + idempotencyKey;
	}
}
