package codehealthy.payflux.walletservice.repositories;

import codehealthy.payflux.walletservice.models.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
	boolean existsByOwnerUserId(Long ownerUserId);

	Optional<Wallet> findByOwnerUserId(Long ownerUserId);

	Optional<Wallet> findByAccountNumber(String accountNumber);

	List<Wallet> findTop100ByOrderByUpdatedAtDesc();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select wallet from Wallet wallet where wallet.ownerUserId = :ownerUserId")
	Optional<Wallet> findByOwnerUserIdForUpdate(Long ownerUserId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select wallet from Wallet wallet where wallet.accountNumber in :accountNumbers order by wallet.accountNumber")
	List<Wallet> findByAccountNumberInForUpdate(Collection<String> accountNumbers);
}
