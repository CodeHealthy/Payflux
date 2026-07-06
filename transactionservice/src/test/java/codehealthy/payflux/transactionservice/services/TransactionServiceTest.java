package codehealthy.payflux.transactionservice.services;

import codehealthy.payflux.transactionservice.dto.TransactionResponse;
import codehealthy.payflux.transactionservice.models.Transaction;
import codehealthy.payflux.transactionservice.repositories.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionServiceTest {

	private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
	private final TransactionService transactionService = new TransactionService(transactionRepository);

	@Test
	void findTransactionDetailsAllowsSender() {
		when(transactionRepository.findByTransactionReference("TRF-1")).thenReturn(Optional.of(transaction()));

		TransactionResponse response = transactionService.findTransactionDetails("TRF-1", 10L, false);

		assertThat(response.transactionReference()).isEqualTo("TRF-1");
		assertThat(response.senderUserId()).isEqualTo(10L);
	}

	@Test
	void findTransactionDetailsAllowsAdmin() {
		when(transactionRepository.findByTransactionReference("TRF-1")).thenReturn(Optional.of(transaction()));

		TransactionResponse response = transactionService.findTransactionDetails("TRF-1", 99L, true);

		assertThat(response.transactionReference()).isEqualTo("TRF-1");
	}

	@Test
	void findTransactionDetailsRejectsUnrelatedUser() {
		when(transactionRepository.findByTransactionReference("TRF-1")).thenReturn(Optional.of(transaction()));

		assertThatThrownBy(() -> transactionService.findTransactionDetails("TRF-1", 99L, false))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Transaction access denied");
	}

	private Transaction transaction() {
		return new Transaction(
				"event-1",
				"TRF-1",
				10L,
				20L,
				"920000000010",
				"920000000020",
				new BigDecimal("1000.00"),
				"PKR",
				"PayFlux transfer",
				Instant.parse("2026-07-06T10:00:00Z")
		);
	}
}
