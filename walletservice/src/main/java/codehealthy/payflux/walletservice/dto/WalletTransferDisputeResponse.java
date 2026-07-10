package codehealthy.payflux.walletservice.dto;

import codehealthy.payflux.walletservice.models.WalletTransfer;
import codehealthy.payflux.walletservice.models.WalletTransferDispute;
import codehealthy.payflux.walletservice.models.WalletTransferDisputeStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletTransferDisputeResponse(
		Long id,
		Long ownerUserId,
		String transactionReference,
		String senderAccountNumber,
		String receiverAccountNumber,
		BigDecimal amount,
		String currency,
		String category,
		String message,
		WalletTransferDisputeStatus status,
		String resolutionNote,
		Long reviewedByUserId,
		Instant reviewedAt,
		Instant resolvedAt,
		Instant createdAt,
		Instant updatedAt
) {
	public static WalletTransferDisputeResponse from(WalletTransferDispute dispute) {
		WalletTransfer transfer = dispute.getTransfer();
		return new WalletTransferDisputeResponse(
				dispute.getId(),
				dispute.getOwnerUserId(),
				dispute.getTransactionReference(),
				transfer.getSenderAccountNumber(),
				transfer.getReceiverAccountNumber(),
				transfer.getAmount(),
				transfer.getCurrency(),
				dispute.getCategory(),
				dispute.getMessage(),
				dispute.getStatus(),
				dispute.getResolutionNote(),
				dispute.getReviewedByUserId(),
				dispute.getReviewedAt(),
				dispute.getResolvedAt(),
				dispute.getCreatedAt(),
				dispute.getUpdatedAt()
		);
	}
}
