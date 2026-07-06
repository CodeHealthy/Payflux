package codehealthy.payflux.authservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class RefreshTokenService {

	private static final String REFRESH_TOKEN_PREFIX = "payflux:auth:refresh-token:";
	private static final int REFRESH_TOKEN_BYTES = 48;

	private final StringRedisTemplate redisTemplate;
	private final SecureRandom secureRandom = new SecureRandom();
	private final Duration refreshTokenTtl;

	public RefreshTokenService(
			StringRedisTemplate redisTemplate,
			@Value("${app.jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays
	) {
		this.redisTemplate = redisTemplate;
		this.refreshTokenTtl = Duration.ofDays(refreshTokenExpirationDays);
	}

	public String issueToken(Long userId) {
		String token = generateOpaqueToken();
		redisTemplate.opsForValue().set(refreshTokenKey(token), userId.toString(), refreshTokenTtl);
		return token;
	}

	public Optional<Long> consumeToken(String refreshToken) {
		String key = refreshTokenKey(refreshToken);
		String userId = redisTemplate.opsForValue().get(key);
		if (userId == null) {
			return Optional.empty();
		}

		redisTemplate.delete(key);
		return Optional.of(Long.parseLong(userId));
	}

	public void revokeToken(String refreshToken) {
		redisTemplate.delete(refreshTokenKey(refreshToken));
	}

	private String generateOpaqueToken() {
		byte[] tokenBytes = new byte[REFRESH_TOKEN_BYTES];
		secureRandom.nextBytes(tokenBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
	}

	private String refreshTokenKey(String token) {
		return REFRESH_TOKEN_PREFIX + sha256(token);
	}

	private String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
		}
	}
}
