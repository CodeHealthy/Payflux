package codehealthy.payflux.walletservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferLimitSummaryResponse(
		String currency,
		BigDecimal singleTransferLimit,
		BigDecimal dailyTransferAmountLimit,
		BigDecimal dailyTransferAmountUsed,
		BigDecimal dailyTransferAmountRemaining,
		long dailyTransferCountLimit,
		long dailyTransferCountUsed,
		long dailyTransferCountRemaining,
		Instant resetAt,
		boolean amountLimitReached,
		boolean countLimitReached
) {
}
