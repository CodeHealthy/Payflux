package codehealthy.payflux.walletservice.dto;

public record CreateTransferDisputeRequest(
		String category,
		String message
) {
}
