package codehealthy.payflux.walletservice.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class WalletTransferTest {

	@Test
	void transferStartsPendingAndMovesThroughProcessingToCompleted() {
		WalletTransfer transfer = transfer();

		assertThat(transfer.getStatus()).isEqualTo(WalletTransferStatus.PENDING_CONFIRMATION);

		transfer.markProcessing();
		assertThat(transfer.getStatus()).isEqualTo(WalletTransferStatus.PROCESSING);

		transfer.markCompleted();
		assertThat(transfer.getStatus()).isEqualTo(WalletTransferStatus.COMPLETED);
		assertThat(transfer.getFailureReason()).isNull();
	}

	@Test
	void failedTransferStoresFailureReason() {
		WalletTransfer transfer = transfer();

		transfer.markProcessing();
		transfer.markFailed("Invalid transfer confirmation code");

		assertThat(transfer.getStatus()).isEqualTo(WalletTransferStatus.FAILED);
		assertThat(transfer.getFailureReason()).isEqualTo("Invalid transfer confirmation code");
	}

	private WalletTransfer transfer() {
		return new WalletTransfer(
				10L,
				"TRF-IDEMPOTENCY-1",
				"idempotency-1",
				"920000000001",
				"920000000002",
				new BigDecimal("1000.00"),
				"PKR",
				"PayFlux transfer"
		);
	}
}
