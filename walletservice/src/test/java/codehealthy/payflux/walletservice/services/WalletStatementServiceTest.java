package codehealthy.payflux.walletservice.services;

import codehealthy.payflux.walletservice.models.Wallet;
import codehealthy.payflux.walletservice.models.WalletLedgerEntry;
import codehealthy.payflux.walletservice.models.WalletLedgerEntryType;
import codehealthy.payflux.walletservice.repositories.WalletLedgerEntryRepository;
import codehealthy.payflux.walletservice.repositories.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WalletStatementServiceTest {

	private final WalletRepository walletRepository = mock(WalletRepository.class);
	private final WalletLedgerEntryRepository ledgerEntryRepository = mock(WalletLedgerEntryRepository.class);
	private final WalletStatementService walletStatementService = new WalletStatementService(walletRepository, ledgerEntryRepository);

	@Test
	void exportCsvUsesSignedAmountsAndEscapesDescriptions() throws Exception {
		Wallet wallet = wallet();
		WalletLedgerEntry credit = entry(wallet, "DEP-1", WalletLedgerEntryType.CREDIT, "1500.00", "1500.00", "Opening deposit");
		WalletLedgerEntry debit = entry(wallet, "TRF-1-D", WalletLedgerEntryType.DEBIT, "250.00", "1250.00", "Transfer, \"school\"");
		setCreatedAt(credit, "2026-07-01T10:15:30Z");
		setCreatedAt(debit, "2026-07-02T11:00:00Z");

		when(walletRepository.findByOwnerUserId(1L)).thenReturn(Optional.of(wallet));
		when(ledgerEntryRepository.findStatementEntries(eq(1L), any(), any())).thenReturn(List.of(credit, debit));

		String csv = walletStatementService.exportCsv(
				1L,
				LocalDate.parse("2026-07-01"),
				LocalDate.parse("2026-07-31")
		);

		assertThat(csv).contains("\"CREDIT\",\"1500.00\",\"PKR\",\"1500.00\",\"Opening deposit\"");
		assertThat(csv).contains("\"DEBIT\",\"-250.00\",\"PKR\",\"1250.00\",\"Transfer, \"\"school\"\"\"");
		verify(ledgerEntryRepository).findStatementEntries(eq(1L), any(), any());
	}

	@Test
	void exportCsvRejectsInvalidDateRange() {
		when(walletRepository.findByOwnerUserId(1L)).thenReturn(Optional.of(wallet()));

		assertThatThrownBy(() -> walletStatementService.exportCsv(
				1L,
				LocalDate.parse("2026-07-31"),
				LocalDate.parse("2026-07-01")
		))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Statement start date must be before end date");
	}

	private Wallet wallet() {
		return new Wallet(1L, 100L, "920000000001", "Ali Khan", "ali@example.com", "PKR");
	}

	private WalletLedgerEntry entry(
			Wallet wallet,
			String reference,
			WalletLedgerEntryType type,
			String amount,
			String balanceAfter,
			String description
	) {
		return new WalletLedgerEntry(
				wallet,
				reference,
				type,
				new BigDecimal(amount),
				new BigDecimal(balanceAfter),
				wallet.getCurrency(),
				description
		);
	}

	private void setCreatedAt(WalletLedgerEntry entry, String createdAt) throws Exception {
		Field createdAtField = WalletLedgerEntry.class.getDeclaredField("createdAt");
		createdAtField.setAccessible(true);
		createdAtField.set(entry, Instant.parse(createdAt));
	}
}
