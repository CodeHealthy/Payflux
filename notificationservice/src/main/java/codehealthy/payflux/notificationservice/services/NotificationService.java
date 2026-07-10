package codehealthy.payflux.notificationservice.services;

import codehealthy.payflux.events.AccountCreatedEvent;
import codehealthy.payflux.events.TransferCompletedEvent;
import codehealthy.payflux.events.TransferDisputeStatusChangedEvent;
import codehealthy.payflux.events.TransferOtpRequestedEvent;
import codehealthy.payflux.notificationservice.dto.NotificationResponse;
import codehealthy.payflux.notificationservice.models.Notification;
import codehealthy.payflux.notificationservice.repositories.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final EmailDeliveryService emailDeliveryService;

	public NotificationService(
			NotificationRepository notificationRepository,
			EmailDeliveryService emailDeliveryService
	) {
		this.notificationRepository = notificationRepository;
		this.emailDeliveryService = emailDeliveryService;
	}

	@Transactional
	public void createWelcomeNotification(AccountCreatedEvent event) {
		String message = "Welcome " + event.fullName() + ". Your account has been created.";
		Notification notification = new Notification(event.accountId(), event.ownerUserId(), event.email(), message);

		notificationRepository.save(notification);
	}

	@Transactional
	public void createTransferOtpNotification(TransferOtpRequestedEvent event) {
		if (notificationRepository.existsByOwnerUserIdAndSourceEventId(event.ownerUserId(), event.eventId())) {
			return;
		}

		String message = "Transfer confirmation code sent for "
				+ event.currency() + " " + event.amount()
				+ " to " + event.receiverAccountNumber() + ".";
		notificationRepository.save(new Notification(
				null,
				event.ownerUserId(),
				event.email(),
				message,
				event.eventId()
		));

		emailDeliveryService.send(
				event.email(),
				"PayFlux transfer confirmation code",
				transferOtpEmailBody(event)
		);
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

	@Transactional
	public void createTransferDisputeNotification(TransferDisputeStatusChangedEvent event) {
		if (notificationRepository.existsByOwnerUserIdAndSourceEventId(event.ownerUserId(), event.eventId())) {
			return;
		}

		notificationRepository.save(new Notification(
				null,
				event.ownerUserId(),
				"wallet-events@payflux.local",
				disputeMessage(event),
				event.eventId()
		));
	}

	@Transactional(readOnly = true)
	public List<NotificationResponse> findNotificationsForUser(Long ownerUserId) {
		return notificationRepository.findByOwnerUserIdOrderByCreatedAtDesc(ownerUserId)
				.stream()
				.map(NotificationResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public long countUnreadNotifications(Long ownerUserId) {
		return notificationRepository.countByOwnerUserIdAndReadAtIsNull(ownerUserId);
	}

	@Transactional
	public NotificationResponse markRead(Long notificationId, Long ownerUserId) {
		Notification notification = notificationRepository.findByIdAndOwnerUserId(notificationId, ownerUserId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

		notification.markRead();
		return NotificationResponse.from(notification);
	}

	@Transactional
	public List<NotificationResponse> markAllRead(Long ownerUserId) {
		notificationRepository.findByOwnerUserIdAndReadAtIsNullOrderByCreatedAtDesc(ownerUserId)
				.forEach(Notification::markRead);
		return findNotificationsForUser(ownerUserId);
	}

	private String transferOtpEmailBody(TransferOtpRequestedEvent event) {
		return """
				PayFlux transfer confirmation

				Use this code to confirm your transfer:

				%s

				Transfer details:
				Amount: %s %s
				Recipient: %s
				Recipient account: %s
				Expires at: %s

				If you did not request this transfer, do not share this code and contact PayFlux support.
				""".formatted(
				event.otp(),
				event.currency(),
				event.amount(),
				event.receiverName(),
				event.receiverAccountNumber(),
				event.expiresAt()
		);
	}

	private String disputeMessage(TransferDisputeStatusChangedEvent event) {
		return switch (event.status()) {
			case "OPEN" -> "Dispute opened for transfer " + event.transactionReference()
					+ ". Operations will review your " + event.category().toLowerCase() + " case.";
			case "UNDER_REVIEW" -> "Dispute " + event.disputeId()
					+ " for transfer " + event.transactionReference() + " is now under review.";
			case "RESOLVED" -> "Dispute " + event.disputeId()
					+ " was resolved. " + optionalText(event.resolutionNote(), "A reversal has been processed.");
			case "REJECTED" -> "Dispute " + event.disputeId()
					+ " was rejected. " + optionalText(event.resolutionNote(), "Operations review found no reversal action.");
			default -> "Dispute " + event.disputeId()
					+ " for transfer " + event.transactionReference() + " changed to " + event.status() + ".";
		};
	}

	private String optionalText(String value, String fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}

		return value.trim();
	}
}
