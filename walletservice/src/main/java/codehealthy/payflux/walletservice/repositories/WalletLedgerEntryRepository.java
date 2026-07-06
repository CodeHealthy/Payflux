package codehealthy.payflux.walletservice.repositories;

import codehealthy.payflux.walletservice.models.WalletLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletLedgerEntryRepository extends JpaRepository<WalletLedgerEntry, Long> {

	boolean existsByWallet_IdAndTransactionReference(Long walletId, String transactionReference);

	List<WalletLedgerEntry> findTop50ByWalletOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);
}
