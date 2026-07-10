package codehealthy.payflux.auditservice.listeners;

import codehealthy.payflux.auditservice.services.AuditService;
import codehealthy.payflux.events.TransferOtpRequestedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransferOtpRequestedEventListener {

	private final AuditService auditService;

	public TransferOtpRequestedEventListener(AuditService auditService) {
		this.auditService = auditService;
	}

	@KafkaListener(topics = "${app.kafka.topics.transfer-otp-requested}", groupId = "${spring.kafka.consumer.group-id}")
	public void handleTransferOtpRequested(TransferOtpRequestedEvent event) {
		auditService.recordTransferOtpRequested(event);
	}
}
