package codehealthy.payflux.notificationservice.repositories;

import codehealthy.payflux.notificationservice.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);

	List<Notification> findByOwnerUserIdAndReadAtIsNullOrderByCreatedAtDesc(Long ownerUserId);

	Optional<Notification> findByIdAndOwnerUserId(Long id, Long ownerUserId);

	boolean existsByOwnerUserIdAndSourceEventId(Long ownerUserId, String sourceEventId);

	long countByOwnerUserIdAndReadAtIsNull(Long ownerUserId);
}
