package codehealthy.payflux.auditservice.listeners;

import codehealthy.payflux.audit.events.AuditTrailEvent;
import codehealthy.payflux.auditservice.services.AuditService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditTrailEventListener {

	private final AuditService auditService;

	public AuditTrailEventListener(AuditService auditService) {
		this.auditService = auditService;
	}

	@KafkaListener(topics = "${app.kafka.topics.audit-events}", groupId = "${spring.kafka.consumer.group-id}")
	public void handleAuditTrailEvent(AuditTrailEvent event) {
		auditService.recordAuditTrailEvent(event);
	}
}
