package codehealthy.payflux.notificationservice.listeners;

import codehealthy.payflux.events.TransferCompletedEvent;
import codehealthy.payflux.notificationservice.services.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransferCompletedEventListener {

	private final NotificationService notificationService;

	public TransferCompletedEventListener(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@KafkaListener(topics = "${app.kafka.topics.transfer-completed}", groupId = "${spring.kafka.consumer.group-id}")
	public void handleTransferCompleted(TransferCompletedEvent event) {
		notificationService.createTransferNotifications(event);
	}
}
