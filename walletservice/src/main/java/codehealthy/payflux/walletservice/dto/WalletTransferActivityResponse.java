package codehealthy.payflux.walletservice.dto;

import codehealthy.payflux.walletservice.models.WalletTransfer;
import codehealthy.payflux.walletservice.models.WalletTransferStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletTransferActivityResponse(
		Long id,
		String transactionReference,
		String senderAccountNumber,
		String receiverAccountNumber,
		BigDecimal amount,
		String currency,
		String description,
		WalletTransferStatus status,
		String failureReason,
		Instant createdAt,
		Instant updatedAt
) {
	public static WalletTransferActivityResponse from(WalletTransfer transfer) {
		return new WalletTransferActivityResponse(
				transfer.getId(),
				transfer.getTransactionReference(),
				transfer.getSenderAccountNumber(),
				transfer.getReceiverAccountNumber(),
				transfer.getAmount(),
				transfer.getCurrency(),
				transfer.getDescription(),
				transfer.getStatus(),
				transfer.getFailureReason(),
				transfer.getCreatedAt(),
				transfer.getUpdatedAt()
		);
	}
}
