package codehealthy.payflux.beneficiaryservice.services;

import codehealthy.payflux.beneficiaryservice.clients.AccountLookupResponse;
import codehealthy.payflux.beneficiaryservice.clients.AccountServiceClient;
import codehealthy.payflux.beneficiaryservice.dto.RecipientVerificationResponse;
import codehealthy.payflux.beneficiaryservice.repositories.BeneficiaryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BeneficiaryServiceTest {

	private final BeneficiaryRepository beneficiaryRepository = mock(BeneficiaryRepository.class);
	private final BeneficiaryEventPublisher beneficiaryEventPublisher = mock(BeneficiaryEventPublisher.class);
	private final AccountServiceClient accountServiceClient = mock(AccountServiceClient.class);
	private final BeneficiaryService beneficiaryService = new BeneficiaryService(
			beneficiaryRepository,
			beneficiaryEventPublisher,
			accountServiceClient
	);

	@Test
	void verifyRecipientReturnsSafeAccountDetails() {
		when(accountServiceClient.findByAccountNumber("920100000001", "token"))
				.thenReturn(new AccountLookupResponse(20L, "920100000001", "Ali Khan"));
		when(beneficiaryRepository.existsByOwnerUserIdAndBeneficiaryAccountNumber(10L, "920100000001"))
				.thenReturn(true);

		RecipientVerificationResponse response = beneficiaryService.verifyRecipient(10L, "token", "920100000001");

		assertThat(response.accountNumber()).isEqualTo("920100000001");
		assertThat(response.displayName()).isEqualTo("Ali K.");
		assertThat(response.institutionName()).isEqualTo("PayFlux");
		assertThat(response.savedBeneficiary()).isTrue();
	}

	@Test
	void verifyRecipientRejectsOwnAccount() {
		when(accountServiceClient.findByAccountNumber("920100000001", "token"))
				.thenReturn(new AccountLookupResponse(10L, "920100000001", "Ali Khan"));

		assertThatThrownBy(() -> beneficiaryService.verifyRecipient(10L, "token", "920100000001"))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("You cannot transfer to your own account");
	}
}
