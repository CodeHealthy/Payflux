package codehealthy.payflux.walletservice.dto;

import java.util.List;

public record WalletDashboardResponse(
		WalletResponse wallet,
		List<WalletTransactionResponse> transactions
) {
}
