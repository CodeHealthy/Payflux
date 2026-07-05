package codehealthy.payflux.transactionservice.dto;

import codehealthy.payflux.transactionservice.models.Transaction;
import codehealthy.payflux.transactionservice.models.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
		Long id,
		String transactionReference,
		Long senderUserId,
		Long receiverUserId,
		String senderAccountNumber,
		String receiverAccountNumber,
		BigDecimal amount,
		String currency,
		String description,
		TransactionStatus status,
		Instant completedAt
) {
	public static TransactionResponse from(Transaction transaction) {
		return new TransactionResponse(
				transaction.getId(),
				transaction.getTransactionReference(),
				transaction.getSenderUserId(),
				transaction.getReceiverUserId(),
				transaction.getSenderAccountNumber(),
				transaction.getReceiverAccountNumber(),
				transaction.getAmount(),
				transaction.getCurrency(),
				transaction.getDescription(),
				transaction.getStatus(),
				transaction.getCompletedAt()
		);
	}
}
