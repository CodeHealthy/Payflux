package codehealthy.payflux.notificationservice.listeners;

import codehealthy.payflux.events.TransferOtpRequestedEvent;
import codehealthy.payflux.notificationservice.services.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransferOtpRequestedEventListener {

	private final NotificationService notificationService;

	public TransferOtpRequestedEventListener(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@KafkaListener(topics = "${app.kafka.topics.transfer-otp-requested}", groupId = "${spring.kafka.consumer.group-id}")
	public void handleTransferOtpRequested(TransferOtpRequestedEvent event) {
		notificationService.createTransferOtpNotification(event);
	}
}
