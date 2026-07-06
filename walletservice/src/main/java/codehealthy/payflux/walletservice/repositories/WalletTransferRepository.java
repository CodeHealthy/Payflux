package codehealthy.payflux.walletservice.repositories;

import codehealthy.payflux.walletservice.models.WalletTransfer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WalletTransferRepository extends JpaRepository<WalletTransfer, Long> {

	Optional<WalletTransfer> findByTransactionReference(String transactionReference);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select transfer from WalletTransfer transfer where transfer.transactionReference = :transactionReference")
	Optional<WalletTransfer> findByTransactionReferenceForUpdate(String transactionReference);

	Optional<WalletTransfer> findByOwnerUserIdAndIdempotencyKey(Long ownerUserId, String idempotencyKey);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select transfer from WalletTransfer transfer where transfer.ownerUserId = :ownerUserId and transfer.idempotencyKey = :idempotencyKey")
	Optional<WalletTransfer> findByOwnerUserIdAndIdempotencyKeyForUpdate(Long ownerUserId, String idempotencyKey);
}
