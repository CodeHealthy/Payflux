package codehealthy.payflux.walletservice.services;

import codehealthy.payflux.events.AdminWalletStatusChangedEvent;
import codehealthy.payflux.events.TransferCompletedEvent;
import codehealthy.payflux.walletservice.models.OutboxEvent;
import codehealthy.payflux.walletservice.repositories.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OutboxService {

	private final OutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;
	private final String transferCompletedTopic;
	private final String adminWalletStatusChangedTopic;

	public OutboxService(
			OutboxEventRepository outboxEventRepository,
			ObjectMapper objectMapper,
			@Value("${app.kafka.topics.transfer-completed}") String transferCompletedTopic,
			@Value("${app.kafka.topics.admin-wallet-status-changed}") String adminWalletStatusChangedTopic
	) {
		this.outboxEventRepository = outboxEventRepository;
		this.objectMapper = objectMapper;
		this.transferCompletedTopic = transferCompletedTopic;
		this.adminWalletStatusChangedTopic = adminWalletStatusChangedTopic;
	}

	public void enqueueTransferCompleted(TransferCompletedEvent event) {
		outboxEventRepository.save(new OutboxEvent(
				event.eventId(),
				transferCompletedTopic,
				TransferCompletedEvent.class.getName(),
				"WALLET_TRANSFER",
				event.transactionReference(),
				toJson(event)
		));
	}

	public void enqueueAdminWalletStatusChanged(AdminWalletStatusChangedEvent event) {
		outboxEventRepository.save(new OutboxEvent(
				event.eventId(),
				adminWalletStatusChangedTopic,
				AdminWalletStatusChangedEvent.class.getName(),
				"WALLET",
				event.ownerUserId().toString(),
				toJson(event)
		));
	}

	private String toJson(Object event) {
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Could not serialize outbox event", exception);
		}
	}
}
