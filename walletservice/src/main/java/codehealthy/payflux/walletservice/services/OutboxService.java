package codehealthy.payflux.walletservice.services;

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

	public OutboxService(
			OutboxEventRepository outboxEventRepository,
			ObjectMapper objectMapper,
			@Value("${app.kafka.topics.transfer-completed}") String transferCompletedTopic
	) {
		this.outboxEventRepository = outboxEventRepository;
		this.objectMapper = objectMapper;
		this.transferCompletedTopic = transferCompletedTopic;
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

	private String toJson(Object event) {
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Could not serialize outbox event", exception);
		}
	}
}
