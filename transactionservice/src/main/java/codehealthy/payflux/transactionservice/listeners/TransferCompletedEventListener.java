package codehealthy.payflux.transactionservice.listeners;

import codehealthy.payflux.events.TransferCompletedEvent;
import codehealthy.payflux.transactionservice.services.TransactionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransferCompletedEventListener {

	private final TransactionService transactionService;

	public TransferCompletedEventListener(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@KafkaListener(topics = "${app.kafka.topics.transfer-completed}", groupId = "${spring.kafka.consumer.group-id}")
	public void handleTransferCompleted(TransferCompletedEvent event) {
		transactionService.recordTransferCompleted(event);
	}
}
