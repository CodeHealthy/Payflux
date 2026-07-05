package codehealthy.payflux.walletservice.dto;

import java.math.BigDecimal;

public record DepositRequest(
		BigDecimal amount,
		String idempotencyKey
) {
}
