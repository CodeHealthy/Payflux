package codehealthy.payflux.auditservice.listeners;

import codehealthy.payflux.auditservice.services.AuditService;
import codehealthy.payflux.authservice.events.UserRegisteredEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredEventListener {

	private final AuditService auditService;

	public UserRegisteredEventListener(AuditService auditService) {
		this.auditService = auditService;
	}

	@KafkaListener(topics = "${app.kafka.topics.user-registered}", groupId = "${spring.kafka.consumer.group-id}")
	public void handleUserRegistered(UserRegisteredEvent event) {
		auditService.recordUserRegistered(event);
	}
}
