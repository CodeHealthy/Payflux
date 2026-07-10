package codehealthy.payflux.authservice.services;

import codehealthy.payflux.audit.events.AuditTrailEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(AuditEventPublisher.class);

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final String auditEventsTopic;

	public AuditEventPublisher(
			KafkaTemplate<String, Object> kafkaTemplate,
			@Value("${app.kafka.topics.audit-events}") String auditEventsTopic
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.auditEventsTopic = auditEventsTopic;
	}

	public void publish(AuditTrailEvent event) {
		kafkaTemplate.send(auditEventsTopic, event.eventId(), event)
				.exceptionally(exception -> {
					log.warn("Could not publish audit event {}", event.eventId(), exception);
					return null;
				});
	}
}
