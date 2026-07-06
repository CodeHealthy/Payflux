package codehealthy.payflux.walletservice.services;

import codehealthy.payflux.walletservice.models.Wallet;
import codehealthy.payflux.walletservice.models.WalletLedgerEntry;
import codehealthy.payflux.walletservice.models.WalletLedgerEntryType;
import codehealthy.payflux.walletservice.repositories.WalletLedgerEntryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class WalletLedgerServiceTest {

	private final WalletLedgerEntryRepository ledgerEntryRepository = mock(WalletLedgerEntryRepository.class);
	private final WalletLedgerService walletLedgerService = new WalletLedgerService(ledgerEntryRepository);

	@Test
	void creditUpdatesWalletProjectionAndStoresLedgerEntry() {
		Wallet wallet = wallet();

		walletLedgerService.credit(wallet, "DEP-1", new BigDecimal("1500.00"), "Funding deposit");

		ArgumentCaptor<WalletLedgerEntry> entryCaptor = ArgumentCaptor.forClass(WalletLedgerEntry.class);
		verify(ledgerEntryRepository).save(entryCaptor.capture());

		WalletLedgerEntry entry = entryCaptor.getValue();
		assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("1500.00");
		assertThat(entry.getEntryType()).isEqualTo(WalletLedgerEntryType.CREDIT);
		assertThat(entry.getAmount()).isEqualByComparingTo("1500.00");
		assertThat(entry.getBalanceAfter()).isEqualByComparingTo("1500.00");
	}

	@Test
	void debitUpdatesWalletProjectionAndStoresLedgerEntry() {
		Wallet wallet = wallet();
		walletLedgerService.credit(wallet, "DEP-1", new BigDecimal("1500.00"), "Funding deposit");

		walletLedgerService.debit(wallet, "TRF-1-D", new BigDecimal("400.00"), "Transfer debit");

		ArgumentCaptor<WalletLedgerEntry> entryCaptor = ArgumentCaptor.forClass(WalletLedgerEntry.class);
		verify(ledgerEntryRepository, times(2)).save(entryCaptor.capture());

		WalletLedgerEntry entry = entryCaptor.getAllValues().get(1);
		assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("1100.00");
		assertThat(entry.getEntryType()).isEqualTo(WalletLedgerEntryType.DEBIT);
		assertThat(entry.getAmount()).isEqualByComparingTo("400.00");
		assertThat(entry.getBalanceAfter()).isEqualByComparingTo("1100.00");
	}

	@Test
	void debitRejectsInsufficientBalanceBeforeSavingLedgerEntry() {
		Wallet wallet = wallet();

		assertThatThrownBy(() -> walletLedgerService.debit(wallet, "TRF-1-D", new BigDecimal("400.00"), "Transfer debit"))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Insufficient wallet balance");

		verify(ledgerEntryRepository, never()).save(any());
	}

	private Wallet wallet() {
		return new Wallet(1L, 100L, "920000000001", "Ali Khan", "ali@example.com", "PKR");
	}
}
