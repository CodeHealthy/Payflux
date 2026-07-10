package codehealthy.payflux.walletservice.repositories;

import codehealthy.payflux.walletservice.models.WalletTransaction;
import codehealthy.payflux.walletservice.models.WalletTransactionStatus;
import codehealthy.payflux.walletservice.models.WalletTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
	boolean existsByTransactionReference(String transactionReference);

	List<WalletTransaction> findTop20ByWalletOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);

	long countByWalletOwnerUserIdAndTypeAndStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
			Long ownerUserId,
			WalletTransactionType type,
			WalletTransactionStatus status,
			Instant from,
			Instant to
	);

	@Query("""
			select coalesce(sum(walletTransaction.amount), 0)
			from WalletTransaction walletTransaction
			where walletTransaction.wallet.ownerUserId = :ownerUserId
				and walletTransaction.type = :type
				and walletTransaction.status = :status
				and walletTransaction.createdAt >= :from
				and walletTransaction.createdAt < :to
			""")
	BigDecimal sumAmountByWalletOwnerUserIdAndTypeAndStatusAndCreatedAtBetween(
			Long ownerUserId,
			WalletTransactionType type,
			WalletTransactionStatus status,
			Instant from,
			Instant to
	);
}
