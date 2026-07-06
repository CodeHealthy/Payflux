package codehealthy.payflux.notificationservice.controllers;

import codehealthy.payflux.notificationservice.dto.NotificationResponse;
import codehealthy.payflux.notificationservice.dto.UnreadNotificationCountResponse;
import codehealthy.payflux.notificationservice.services.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping
	public List<NotificationResponse> findCurrentUserNotifications(@AuthenticationPrincipal Jwt jwt) {
		return notificationService.findNotificationsForUser(currentUserId(jwt));
	}

	@GetMapping("/unread-count")
	public UnreadNotificationCountResponse countUnreadNotifications(@AuthenticationPrincipal Jwt jwt) {
		return new UnreadNotificationCountResponse(notificationService.countUnreadNotifications(currentUserId(jwt)));
	}

	@PatchMapping("/{notificationId}/read")
	public NotificationResponse markRead(@AuthenticationPrincipal Jwt jwt, @PathVariable Long notificationId) {
		return notificationService.markRead(notificationId, currentUserId(jwt));
	}

	@PatchMapping("/read-all")
	public List<NotificationResponse> markAllRead(@AuthenticationPrincipal Jwt jwt) {
		return notificationService.markAllRead(currentUserId(jwt));
	}

	private Long currentUserId(Jwt jwt) {
		Number userId = jwt.getClaim("userId");
		if (userId == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is missing userId");
		}

		return userId.longValue();
	}
}
