package codehealthy.payflux.notificationservice.repositories;

import codehealthy.payflux.notificationservice.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);

	boolean existsByOwnerUserIdAndSourceEventId(Long ownerUserId, String sourceEventId);
}
