package codehealthy.payflux.notificationservice.dto;

import codehealthy.payflux.notificationservice.models.Notification;

import java.time.Instant;

public record NotificationResponse(
		Long id,
		Long accountId,
		Long ownerUserId,
		String email,
		String message,
		Instant createdAt
) {
	public static NotificationResponse from(Notification notification) {
		return new NotificationResponse(
				notification.getId(),
				notification.getAccountId(),
				notification.getOwnerUserId(),
				notification.getEmail(),
				notification.getMessage(),
				notification.getCreatedAt()
		);
	}
}
