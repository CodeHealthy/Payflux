package codehealthy.payflux.walletservice.repositories;

import codehealthy.payflux.walletservice.models.WalletLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface WalletLedgerEntryRepository extends JpaRepository<WalletLedgerEntry, Long> {

	boolean existsByWallet_IdAndTransactionReference(Long walletId, String transactionReference);

	List<WalletLedgerEntry> findTop50ByWalletOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);

	@Query("""
			select entry from WalletLedgerEntry entry
			where entry.wallet.ownerUserId = :ownerUserId
				and entry.createdAt >= :from
				and entry.createdAt < :to
			order by entry.createdAt asc
			""")
	List<WalletLedgerEntry> findStatementEntries(Long ownerUserId, Instant from, Instant to);
}
