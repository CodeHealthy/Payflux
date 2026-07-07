package codehealthy.payflux.walletservice.services;

import codehealthy.payflux.walletservice.dto.PendingTransfer;
import codehealthy.payflux.walletservice.dto.TransferConfirmationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransferConfirmationService {

	private static final String KEY_PREFIX = "payflux:transfer-confirmation:";
	private static final String ATTEMPT_KEY_PREFIX = "payflux:transfer-confirmation-attempts:";
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private final Duration confirmationTtl;
	private final int maxOtpAttempts;

	public TransferConfirmationService(
			StringRedisTemplate redisTemplate,
			ObjectMapper objectMapper,
			@Value("${app.transfer.confirmation-ttl-seconds}") long confirmationTtlSeconds,
			@Value("${app.transfer.max-otp-attempts}") int maxOtpAttempts
	) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
		this.confirmationTtl = Duration.ofSeconds(confirmationTtlSeconds);
		this.maxOtpAttempts = maxOtpAttempts;
	}

	public TransferConfirmationResponse create(PendingTransfer pendingTransfer) {
		String payload = toJson(pendingTransfer);
		redisTemplate.opsForValue().set(key(pendingTransfer.ownerUserId(), pendingTransfer.confirmationId()), payload, confirmationTtl);

		return new TransferConfirmationResponse(
				pendingTransfer.confirmationId(),
				pendingTransfer.receiverAccountNumber(),
				pendingTransfer.receiverName(),
				pendingTransfer.amount(),
				pendingTransfer.currency(),
				pendingTransfer.description(),
				pendingTransfer.expiresAt(),
				pendingTransfer.idempotencyKey()
		);
	}

	public PendingTransfer consume(Long ownerUserId, String confirmationId, String otp) {
		String key = key(ownerUserId, requireText(confirmationId, "confirmationId"));
		String payload = redisTemplate.opsForValue().get(key);
		if (payload == null) {
			throw new ResponseStatusException(HttpStatus.GONE, "Transfer confirmation expired");
		}

		PendingTransfer pendingTransfer = fromJson(payload);
		if (!pendingTransfer.otp().equals(requireText(otp, "otp"))) {
			registerFailedOtpAttempt(ownerUserId, pendingTransfer.confirmationId());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid transfer confirmation code");
		}

		redisTemplate.delete(key);
		redisTemplate.delete(attemptKey(ownerUserId, pendingTransfer.confirmationId()));
		return pendingTransfer;
	}

	public PendingTransfer pendingTransfer(
			Long ownerUserId,
			String email,
			String senderAccountNumber,
			String receiverAccountNumber,
			String receiverName,
			java.math.BigDecimal amount,
			String currency,
			String description,
			String idempotencyKey
	) {
		String confirmationId = UUID.randomUUID().toString();
		return new PendingTransfer(
				confirmationId,
				ownerUserId,
				email,
				senderAccountNumber,
				receiverAccountNumber,
				receiverName,
				amount,
				currency,
				description,
				idempotencyKey,
				otp(),
				Instant.now().plus(confirmationTtl)
		);
	}

	private String key(Long ownerUserId, String confirmationId) {
		return KEY_PREFIX + ownerUserId + ":" + confirmationId;
	}

	private String attemptKey(Long ownerUserId, String confirmationId) {
		return ATTEMPT_KEY_PREFIX + ownerUserId + ":" + confirmationId;
	}

	private void registerFailedOtpAttempt(Long ownerUserId, String confirmationId) {
		String attemptKey = attemptKey(ownerUserId, confirmationId);
		Long attempts = redisTemplate.opsForValue().increment(attemptKey);
		if (attempts != null && attempts == 1) {
			redisTemplate.expire(attemptKey, confirmationTtl);
		}

		if (attempts != null && attempts >= maxOtpAttempts) {
			redisTemplate.delete(key(ownerUserId, confirmationId));
			redisTemplate.delete(attemptKey);
			throw new ResponseStatusException(
					HttpStatus.LOCKED,
					"Transfer confirmation locked after too many invalid codes"
			);
		}
	}

	private String otp() {
		return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
	}

	private String toJson(PendingTransfer pendingTransfer) {
		try {
			return objectMapper.writeValueAsString(pendingTransfer);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Could not serialize pending transfer", exception);
		}
	}

	private PendingTransfer fromJson(String payload) {
		try {
			return objectMapper.readValue(payload, PendingTransfer.class);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Could not read pending transfer", exception);
		}
	}

	private String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
		}

		return value.trim();
	}
}
