package codehealthy.payflux.walletservice.repositories;

import codehealthy.payflux.walletservice.models.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
	boolean existsByTransactionReference(String transactionReference);

	List<WalletTransaction> findTop20ByWalletOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);
}
