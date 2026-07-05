package codehealthy.payflux.walletservice.dto;

import codehealthy.payflux.walletservice.models.WalletTransaction;
import codehealthy.payflux.walletservice.models.WalletTransactionStatus;
import codehealthy.payflux.walletservice.models.WalletTransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletTransactionResponse(
		Long id,
		String transactionReference,
		WalletTransactionType type,
		WalletTransactionStatus status,
		BigDecimal amount,
		String currency,
		String description,
		String counterpartyAccountNumber,
		Instant createdAt
) {
	public static WalletTransactionResponse from(WalletTransaction transaction) {
		return new WalletTransactionResponse(
				transaction.getId(),
				transaction.getTransactionReference(),
				transaction.getType(),
				transaction.getStatus(),
				transaction.getAmount(),
				transaction.getCurrency(),
				transaction.getDescription(),
				transaction.getCounterpartyAccountNumber(),
				transaction.getCreatedAt()
		);
	}
}
