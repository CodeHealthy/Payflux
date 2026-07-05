package codehealthy.payflux.notificationservice.services;

import codehealthy.payflux.events.AccountCreatedEvent;
import codehealthy.payflux.events.TransferCompletedEvent;
import codehealthy.payflux.notificationservice.dto.NotificationResponse;
import codehealthy.payflux.notificationservice.models.Notification;
import codehealthy.payflux.notificationservice.repositories.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;

	public NotificationService(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	@Transactional
	public void createWelcomeNotification(AccountCreatedEvent event) {
		String message = "Welcome " + event.fullName() + ". Your account has been created.";
		Notification notification = new Notification(event.accountId(), event.ownerUserId(), event.email(), message);

		notificationRepository.save(notification);
	}

	@Transactional
	public void createTransferNotifications(TransferCompletedEvent event) {
		if (notificationRepository.existsByOwnerUserIdAndSourceEventId(event.senderUserId(), event.eventId())
				&& notificationRepository.existsByOwnerUserIdAndSourceEventId(event.receiverUserId(), event.eventId())) {
			return;
		}

		String senderMessage = "Transfer " + event.transactionReference() + " sent "
				+ event.currency() + " " + event.amount() + " to " + event.receiverAccountNumber() + ".";
		String receiverMessage = "Transfer " + event.transactionReference() + " received "
				+ event.currency() + " " + event.amount() + " from " + event.senderAccountNumber() + ".";

		if (!notificationRepository.existsByOwnerUserIdAndSourceEventId(event.senderUserId(), event.eventId())) {
			notificationRepository.save(new Notification(
					null,
					event.senderUserId(),
					"wallet-events@payflux.local",
					senderMessage,
					event.eventId()
			));
		}

		if (!notificationRepository.existsByOwnerUserIdAndSourceEventId(event.receiverUserId(), event.eventId())) {
			notificationRepository.save(new Notification(
					null,
					event.receiverUserId(),
					"wallet-events@payflux.local",
					receiverMessage,
					event.eventId()
			));
		}
	}

	@Transactional(readOnly = true)
	public List<NotificationResponse> findNotificationsForUser(Long ownerUserId) {
		return notificationRepository.findByOwnerUserIdOrderByCreatedAtDesc(ownerUserId)
				.stream()
				.map(NotificationResponse::from)
				.toList();
	}
}
