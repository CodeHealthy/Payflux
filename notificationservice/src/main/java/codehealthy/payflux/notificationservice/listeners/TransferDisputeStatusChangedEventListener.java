package codehealthy.payflux.notificationservice.listeners;

import codehealthy.payflux.events.TransferDisputeStatusChangedEvent;
import codehealthy.payflux.notificationservice.services.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransferDisputeStatusChangedEventListener {

	private final NotificationService notificationService;

	public TransferDisputeStatusChangedEventListener(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@KafkaListener(topics = "${app.kafka.topics.transfer-dispute-status-changed}", groupId = "${spring.kafka.consumer.group-id}")
	public void handleTransferDisputeStatusChanged(TransferDisputeStatusChangedEvent event) {
		notificationService.createTransferDisputeNotification(event);
	}
}
