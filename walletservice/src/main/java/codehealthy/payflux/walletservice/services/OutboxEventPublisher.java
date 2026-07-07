package codehealthy.payflux.walletservice.services;

import codehealthy.payflux.events.AdminWalletStatusChangedEvent;
import codehealthy.payflux.events.TransferCompletedEvent;
import codehealthy.payflux.events.TransferOtpRequestedEvent;
import codehealthy.payflux.walletservice.models.OutboxEvent;
import codehealthy.payflux.walletservice.models.OutboxEventStatus;
import codehealthy.payflux.walletservice.repositories.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Component
public class OutboxEventPublisher {

	private static final int MAX_ATTEMPTS = 10;

	private final OutboxEventRepository outboxEventRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public OutboxEventPublisher(
			OutboxEventRepository outboxEventRepository,
			KafkaTemplate<String, Object> kafkaTemplate,
			ObjectMapper objectMapper
	) {
		this.outboxEventRepository = outboxEventRepository;
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
	}

	@Scheduled(fixedDelayString = "${app.outbox.publish-delay-ms:3000}")
	@Transactional
	public void publishPendingEvents() {
		outboxEventRepository
				.findTop25ByStatusAndAttemptsLessThanOrderByCreatedAtAsc(OutboxEventStatus.PENDING, MAX_ATTEMPTS)
				.forEach(this::publish);
	}

	private void publish(OutboxEvent outboxEvent) {
		try {
			Object event = deserialize(outboxEvent);
			kafkaTemplate.send(outboxEvent.getTopic(), outboxEvent.getEventId(), event)
					.get(Duration.ofSeconds(5).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
			outboxEvent.markPublished();
		} catch (Exception exception) {
			outboxEvent.markFailed(exception.getMessage());
			if (outboxEvent.getAttempts() < MAX_ATTEMPTS) {
				outboxEvent.markPendingForRetry();
			}
		}
	}

	private Object deserialize(OutboxEvent outboxEvent) throws Exception {
		if (TransferCompletedEvent.class.getName().equals(outboxEvent.getEventType())) {
			return objectMapper.readValue(outboxEvent.getPayload(), TransferCompletedEvent.class);
		}
		if (AdminWalletStatusChangedEvent.class.getName().equals(outboxEvent.getEventType())) {
			return objectMapper.readValue(outboxEvent.getPayload(), AdminWalletStatusChangedEvent.class);
		}
		if (TransferOtpRequestedEvent.class.getName().equals(outboxEvent.getEventType())) {
			return objectMapper.readValue(outboxEvent.getPayload(), TransferOtpRequestedEvent.class);
		}

		throw new IllegalArgumentException("Unsupported outbox event type " + outboxEvent.getEventType());
	}
}
