package codehealthy.payflux.walletservice.services;

import codehealthy.payflux.walletservice.dto.PendingTransfer;
import codehealthy.payflux.walletservice.dto.TransferConfirmationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferConfirmationServiceTest {

	private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
	private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
	private final TransferConfirmationService service = new TransferConfirmationService(redisTemplate, objectMapper, 300, 3);

	@Test
	void invalidOtpIsBusinessValidationFailureNotAuthFailure() throws Exception {
		PendingTransfer pendingTransfer = new PendingTransfer(
				"confirmation-1",
				10L,
				"ali@example.com",
				"920000000001",
				"920000000002",
				"Ali Khan",
				new BigDecimal("1000.00"),
				"PKR",
				"PayFlux transfer",
				"idempotency-1",
				"123456",
				Instant.now().plusSeconds(300)
		);

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(any())).thenReturn(objectMapper.writeValueAsString(pendingTransfer));

		assertThatThrownBy(() -> service.consume(10L, "confirmation-1", "999999"))
				.isInstanceOfSatisfying(ResponseStatusException.class, exception -> {
					assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
					assertThat(exception.getReason()).isEqualTo("Invalid transfer confirmation code");
				});

		verify(valueOperations).increment(eq("payflux:transfer-confirmation-attempts:10:confirmation-1"));
	}

	@Test
	void validOtpConsumesPendingTransfer() throws Exception {
		PendingTransfer pendingTransfer = new PendingTransfer(
				"confirmation-1",
				10L,
				"ali@example.com",
				"920000000001",
				"920000000002",
				"Ali Khan",
				new BigDecimal("1000.00"),
				"PKR",
				"PayFlux transfer",
				"idempotency-1",
				"123456",
				Instant.now().plusSeconds(300)
		);

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(any())).thenReturn(objectMapper.writeValueAsString(pendingTransfer));

		PendingTransfer consumedTransfer = service.consume(10L, "confirmation-1", "123456");

		assertThat(consumedTransfer).isEqualTo(pendingTransfer);
		verify(redisTemplate).delete("payflux:transfer-confirmation:10:confirmation-1");
		verify(redisTemplate).delete("payflux:transfer-confirmation-attempts:10:confirmation-1");
	}
}
