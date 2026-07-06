package codehealthy.payflux.notificationservice.services;

import codehealthy.payflux.notificationservice.dto.NotificationResponse;
import codehealthy.payflux.notificationservice.models.Notification;
import codehealthy.payflux.notificationservice.repositories.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

	private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
	private final NotificationService notificationService = new NotificationService(notificationRepository);

	@Test
	void markReadSetsReadAtForOwnerNotification() {
		Notification notification = notification();
		when(notificationRepository.findByIdAndOwnerUserId(1L, 10L)).thenReturn(Optional.of(notification));

		NotificationResponse response = notificationService.markRead(1L, 10L);

		assertThat(response.unread()).isFalse();
		assertThat(response.readAt()).isNotNull();
	}

	@Test
	void markReadRejectsNotificationOwnedByAnotherUser() {
		when(notificationRepository.findByIdAndOwnerUserId(1L, 99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> notificationService.markRead(1L, 99L))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Notification not found");
	}

	@Test
	void markAllReadReturnsUpdatedNotifications() {
		Notification notification = notification();
		notification.markRead();
		when(notificationRepository.findByOwnerUserIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(notification));

		List<NotificationResponse> notifications = notificationService.markAllRead(10L);

		assertThat(notifications).hasSize(1);
		assertThat(notifications.get(0).unread()).isFalse();
	}

	private Notification notification() {
		return new Notification(100L, 10L, "ali@example.com", "Welcome Ali. Your account has been created.");
	}
}
