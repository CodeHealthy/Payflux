package codehealthy.payflux.walletservice.repositories;

import codehealthy.payflux.walletservice.models.WalletTransferDispute;
import codehealthy.payflux.walletservice.models.WalletTransferDisputeStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WalletTransferDisputeRepository extends JpaRepository<WalletTransferDispute, Long> {
	boolean existsByTransactionReferenceAndOwnerUserIdAndStatusIn(
			String transactionReference,
			Long ownerUserId,
			Collection<WalletTransferDisputeStatus> statuses
	);

	List<WalletTransferDispute> findTop50ByOwnerUserIdOrderByUpdatedAtDesc(Long ownerUserId);

	List<WalletTransferDispute> findTop100ByOrderByUpdatedAtDesc();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select dispute from WalletTransferDispute dispute where dispute.id = :id")
	Optional<WalletTransferDispute> findByIdForUpdate(Long id);
}
