package codehealthy.payflux.transactionservice.repositories;

import codehealthy.payflux.transactionservice.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	boolean existsByEventId(String eventId);

	List<Transaction> findTop50BySenderUserIdOrReceiverUserIdOrderByCompletedAtDesc(Long senderUserId, Long receiverUserId);
}
