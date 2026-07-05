package codehealthy.payflux.accountservice.repositories;

import codehealthy.payflux.accountservice.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
	boolean existsByOwnerUserId(Long ownerUserId);

	boolean existsByAccountNumber(String accountNumber);

	List<Account> findByOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);

	Optional<Account> findByAccountNumber(String accountNumber);
}
