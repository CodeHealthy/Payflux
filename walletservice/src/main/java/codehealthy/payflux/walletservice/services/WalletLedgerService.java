package codehealthy.payflux.walletservice.services;

import codehealthy.payflux.walletservice.models.Wallet;
import codehealthy.payflux.walletservice.models.WalletLedgerEntry;
import codehealthy.payflux.walletservice.models.WalletLedgerEntryType;
import codehealthy.payflux.walletservice.repositories.WalletLedgerEntryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class WalletLedgerService {

	private final WalletLedgerEntryRepository ledgerEntryRepository;

	public WalletLedgerService(WalletLedgerEntryRepository ledgerEntryRepository) {
		this.ledgerEntryRepository = ledgerEntryRepository;
	}

	public void credit(Wallet wallet, String transactionReference, BigDecimal amount, String description) {
		rejectDuplicateLedgerEntry(wallet, transactionReference);
		wallet.credit(amount);
		saveEntry(wallet, transactionReference, WalletLedgerEntryType.CREDIT, amount, description);
	}

	public void debit(Wallet wallet, String transactionReference, BigDecimal amount, String description) {
		rejectDuplicateLedgerEntry(wallet, transactionReference);
		if (wallet.getAvailableBalance().compareTo(amount) < 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient wallet balance");
		}

		wallet.debit(amount);
		saveEntry(wallet, transactionReference, WalletLedgerEntryType.DEBIT, amount, description);
	}

	private void saveEntry(
			Wallet wallet,
			String transactionReference,
			WalletLedgerEntryType entryType,
			BigDecimal amount,
			String description
	) {
		ledgerEntryRepository.save(new WalletLedgerEntry(
				wallet,
				transactionReference,
				entryType,
				amount,
				wallet.getAvailableBalance(),
				wallet.getCurrency(),
				description
		));
	}

	private void rejectDuplicateLedgerEntry(Wallet wallet, String transactionReference) {
		if (wallet.getId() != null && ledgerEntryRepository.existsByWallet_IdAndTransactionReference(wallet.getId(), transactionReference)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate wallet ledger entry");
		}
	}
}
