package codehealthy.payflux.auditservice.listeners;

import codehealthy.payflux.auditservice.services.AuditService;
import codehealthy.payflux.events.AdminWalletStatusChangedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AdminWalletStatusChangedEventListener {

	private final AuditService auditService;

	public AdminWalletStatusChangedEventListener(AuditService auditService) {
		this.auditService = auditService;
	}

	@KafkaListener(topics = "${app.kafka.topics.admin-wallet-status-changed}", groupId = "${spring.kafka.consumer.group-id}")
	public void handleAdminWalletStatusChanged(AdminWalletStatusChangedEvent event) {
		auditService.recordAdminWalletStatusChanged(event);
	}
}
