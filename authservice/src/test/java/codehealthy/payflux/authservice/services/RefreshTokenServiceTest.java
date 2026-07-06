package codehealthy.payflux.authservice.services;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

	private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
	private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
	private final RefreshTokenService refreshTokenService = new RefreshTokenService(redisTemplate, 7);

	@Test
	void issueTokenStoresHashedTokenWithUserIdAndTtl() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);

		String refreshToken = refreshTokenService.issueToken(42L);

		ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
		verify(valueOperations).set(keyCaptor.capture(), eq("42"), eq(Duration.ofDays(7)));

		assertThat(refreshToken).isNotBlank();
		assertThat(keyCaptor.getValue())
				.startsWith("payflux:auth:refresh-token:")
				.doesNotContain(refreshToken);
	}

	@Test
	void consumeTokenDeletesStoredTokenAfterSuccessfulLookup() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(any())).thenReturn("42");

		Optional<Long> userId = refreshTokenService.consumeToken("refresh-token");

		assertThat(userId).contains(42L);
		verify(redisTemplate).delete(any(String.class));
	}

	@Test
	void consumeTokenReturnsEmptyWhenTokenIsMissing() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(any())).thenReturn(null);

		Optional<Long> userId = refreshTokenService.consumeToken("missing-token");

		assertThat(userId).isEmpty();
	}
}
