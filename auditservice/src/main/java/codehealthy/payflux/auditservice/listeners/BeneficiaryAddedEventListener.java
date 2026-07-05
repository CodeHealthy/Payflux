package codehealthy.payflux.auditservice.listeners;

import codehealthy.payflux.auditservice.services.AuditService;
import codehealthy.payflux.beneficiaryservice.events.BeneficiaryAddedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BeneficiaryAddedEventListener {

	private final AuditService auditService;

	public BeneficiaryAddedEventListener(AuditService auditService) {
		this.auditService = auditService;
	}

	@KafkaListener(topics = "${app.kafka.topics.beneficiary-added}", groupId = "${spring.kafka.consumer.group-id}")
	public void handleBeneficiaryAdded(BeneficiaryAddedEvent event) {
		auditService.recordBeneficiaryAdded(event);
	}
}
