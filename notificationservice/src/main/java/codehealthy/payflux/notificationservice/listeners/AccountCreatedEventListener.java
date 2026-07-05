package codehealthy.payflux.notificationservice.listeners;

import codehealthy.payflux.events.AccountCreatedEvent;
import codehealthy.payflux.notificationservice.services.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AccountCreatedEventListener {

	private final NotificationService notificationService;

	public AccountCreatedEventListener(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@KafkaListener(topics = "${app.kafka.topics.account-created}", groupId = "${spring.kafka.consumer.group-id}")
	public void handleAccountCreated(AccountCreatedEvent event) {
		notificationService.createWelcomeNotification(event);
	}
}
