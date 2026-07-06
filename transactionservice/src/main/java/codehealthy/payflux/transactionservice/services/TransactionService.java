package codehealthy.payflux.transactionservice.services;

import codehealthy.payflux.events.TransferCompletedEvent;
import codehealthy.payflux.transactionservice.dto.TransactionResponse;
import codehealthy.payflux.transactionservice.models.Transaction;
import codehealthy.payflux.transactionservice.repositories.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TransactionService {

	private final TransactionRepository transactionRepository;

	public TransactionService(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	@Transactional
	public void recordTransferCompleted(TransferCompletedEvent event) {
		if (transactionRepository.existsByEventId(event.eventId())) {
			return;
		}

		transactionRepository.save(new Transaction(
				event.eventId(),
				event.transactionReference(),
				event.senderUserId(),
				event.receiverUserId(),
				event.senderAccountNumber(),
				event.receiverAccountNumber(),
				event.amount(),
				event.currency(),
				event.description(),
				event.completedAt()
		));
	}

	@Transactional(readOnly = true)
	public List<TransactionResponse> findCurrentUserTransactions(Long ownerUserId) {
		return transactionRepository
				.findTop50BySenderUserIdOrReceiverUserIdOrderByCompletedAtDesc(ownerUserId, ownerUserId)
				.stream()
				.map(TransactionResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public TransactionResponse findTransactionDetails(String transactionReference, Long ownerUserId, boolean admin) {
		Transaction transaction = transactionRepository.findByTransactionReference(transactionReference)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

		if (!admin && !belongsToUser(transaction, ownerUserId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Transaction access denied");
		}

		return TransactionResponse.from(transaction);
	}

	private boolean belongsToUser(Transaction transaction, Long ownerUserId) {
		return transaction.getSenderUserId().equals(ownerUserId) || transaction.getReceiverUserId().equals(ownerUserId);
	}
}
