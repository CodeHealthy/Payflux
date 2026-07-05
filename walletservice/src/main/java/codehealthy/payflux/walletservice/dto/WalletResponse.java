package codehealthy.payflux.walletservice.dto;

import codehealthy.payflux.walletservice.models.Wallet;
import codehealthy.payflux.walletservice.models.WalletStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletResponse(
		Long id,
		Long ownerUserId,
		String accountNumber,
		String fullName,
		String email,
		String currency,
		BigDecimal availableBalance,
		WalletStatus status,
		Instant createdAt,
		Instant updatedAt
) {
	public static WalletResponse from(Wallet wallet) {
		return new WalletResponse(
				wallet.getId(),
				wallet.getOwnerUserId(),
				wallet.getAccountNumber(),
				wallet.getFullName(),
				wallet.getEmail(),
				wallet.getCurrency(),
				wallet.getAvailableBalance(),
				wallet.getStatus(),
				wallet.getCreatedAt(),
				wallet.getUpdatedAt()
		);
	}
}
