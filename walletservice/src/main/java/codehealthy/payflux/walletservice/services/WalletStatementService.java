package codehealthy.payflux.walletservice.services;

import codehealthy.payflux.walletservice.models.WalletLedgerEntry;
import codehealthy.payflux.walletservice.models.WalletLedgerEntryType;
import codehealthy.payflux.walletservice.repositories.WalletLedgerEntryRepository;
import codehealthy.payflux.walletservice.repositories.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class WalletStatementService {

	private static final int DEFAULT_STATEMENT_DAYS = 30;
	private static final int MAX_STATEMENT_DAYS = 366;

	private final WalletRepository walletRepository;
	private final WalletLedgerEntryRepository ledgerEntryRepository;

	public WalletStatementService(
			WalletRepository walletRepository,
			WalletLedgerEntryRepository ledgerEntryRepository
	) {
		this.walletRepository = walletRepository;
		this.ledgerEntryRepository = ledgerEntryRepository;
	}

	public String exportCsv(Long ownerUserId, LocalDate requestedFrom, LocalDate requestedTo) {
		walletRepository.findByOwnerUserId(ownerUserId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet is not ready yet"));

		LocalDate today = LocalDate.now(ZoneOffset.UTC);
		LocalDate to = requestedTo == null ? today : requestedTo;
		LocalDate from = requestedFrom == null ? to.minusDays(DEFAULT_STATEMENT_DAYS) : requestedFrom;
		validateRange(from, to);

		List<WalletLedgerEntry> entries = ledgerEntryRepository.findStatementEntries(
				ownerUserId,
				from.atStartOfDay().toInstant(ZoneOffset.UTC),
				to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
		);

		return buildCsv(entries);
	}

	private void validateRange(LocalDate from, LocalDate to) {
		if (from.isAfter(to)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statement start date must be before end date");
		}

		if (ChronoUnit.DAYS.between(from, to) > MAX_STATEMENT_DAYS) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statement date range cannot exceed 366 days");
		}
	}

	private String buildCsv(List<WalletLedgerEntry> entries) {
		StringBuilder csv = new StringBuilder();
		csv.append("Date,Reference,Type,Amount,Currency,Balance After,Description\n");

		for (WalletLedgerEntry entry : entries) {
			csv.append(csvValue(entry.getCreatedAt().toString())).append(",");
			csv.append(csvValue(entry.getTransactionReference())).append(",");
			csv.append(csvValue(entry.getEntryType().name())).append(",");
			csv.append(csvValue(signedAmount(entry))).append(",");
			csv.append(csvValue(entry.getCurrency())).append(",");
			csv.append(csvValue(entry.getBalanceAfter().toPlainString())).append(",");
			csv.append(csvValue(entry.getDescription())).append("\n");
		}

		return csv.toString();
	}

	private String signedAmount(WalletLedgerEntry entry) {
		BigDecimal amount = entry.getEntryType() == WalletLedgerEntryType.DEBIT
				? entry.getAmount().negate()
				: entry.getAmount();

		return amount.toPlainString();
	}

	private String csvValue(String value) {
		String safeValue = value == null ? "" : value;
		return "\"" + safeValue.replace("\"", "\"\"") + "\"";
	}
}
