package codehealthy.payflux.walletservice.repositories;

import codehealthy.payflux.walletservice.models.OutboxEvent;
import codehealthy.payflux.walletservice.models.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
	List<OutboxEvent> findTop25ByStatusAndAttemptsLessThanOrderByCreatedAtAsc(OutboxEventStatus status, int maxAttempts);
}
