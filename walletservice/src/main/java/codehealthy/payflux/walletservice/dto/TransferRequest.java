package codehealthy.payflux.walletservice.dto;

import java.math.BigDecimal;

public record TransferRequest(
		String receiverAccountNumber,
		BigDecimal amount,
		String description,
		String idempotencyKey
) {
}
