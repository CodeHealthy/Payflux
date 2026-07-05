package codehealthy.payflux.auditservice.listeners;

import codehealthy.payflux.auditservice.services.AuditService;
import codehealthy.payflux.events.TransferCompletedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransferCompletedEventListener {

	private final AuditService auditService;

	public TransferCompletedEventListener(AuditService auditService) {
		this.auditService = auditService;
	}

	@KafkaListener(topics = "${app.kafka.topics.transfer-completed}", groupId = "${spring.kafka.consumer.group-id}")
	public void handleTransferCompleted(TransferCompletedEvent event) {
		auditService.recordTransferCompleted(event);
	}
}
