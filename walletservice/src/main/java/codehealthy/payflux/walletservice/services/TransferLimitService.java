package codehealthy.payflux.walletservice.services;

import codehealthy.payflux.walletservice.dto.TransferLimitSummaryResponse;
import codehealthy.payflux.walletservice.models.Wallet;
import codehealthy.payflux.walletservice.models.WalletTransactionStatus;
import codehealthy.payflux.walletservice.models.WalletTransactionType;
import codehealthy.payflux.walletservice.repositories.WalletRepository;
import codehealthy.payflux.walletservice.repositories.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class TransferLimitService {

	private final WalletTransactionRepository transactionRepository;
	private final WalletRepository walletRepository;
	private final BigDecimal singleTransferLimit;
	private final BigDecimal dailyTransferAmountLimit;
	private final long dailyTransferCountLimit;
	private final Clock clock;

	@Autowired
	public TransferLimitService(
			WalletTransactionRepository transactionRepository,
			WalletRepository walletRepository,
			@Value("${app.transfer.limits.single-transfer-amount}") BigDecimal singleTransferLimit,
			@Value("${app.transfer.limits.daily-total-amount}") BigDecimal dailyTransferAmountLimit,
			@Value("${app.transfer.limits.daily-count}") long dailyTransferCountLimit
	) {
		this(transactionRepository, walletRepository, singleTransferLimit, dailyTransferAmountLimit, dailyTransferCountLimit, Clock.systemUTC());
	}

	TransferLimitService(
			WalletTransactionRepository transactionRepository,
			WalletRepository walletRepository,
			BigDecimal singleTransferLimit,
			BigDecimal dailyTransferAmountLimit,
			long dailyTransferCountLimit,
			Clock clock
	) {
		this.transactionRepository = transactionRepository;
		this.walletRepository = walletRepository;
		this.singleTransferLimit = money(singleTransferLimit);
		this.dailyTransferAmountLimit = money(dailyTransferAmountLimit);
		this.dailyTransferCountLimit = dailyTransferCountLimit;
		this.clock = clock;
	}

	public void assertAllowed(Long ownerUserId, BigDecimal amount, String currency) {
		BigDecimal transferAmount = money(amount);
		if (transferAmount.compareTo(singleTransferLimit) > 0) {
			throw limitExceeded("Transfer amount exceeds the single transfer limit of "
					+ currency + " " + singleTransferLimit.toPlainString());
		}

		Instant dayStart = todayStart();
		Instant dayEnd = nextDayStart(dayStart);
		long dailyCount = dailyTransferCount(ownerUserId, dayStart, dayEnd);
		if (dailyCount >= dailyTransferCountLimit) {
			throw limitExceeded("Daily transfer count limit reached. Try again tomorrow.");
		}

		BigDecimal dailyTotal = dailyTransferAmount(ownerUserId, dayStart, dayEnd);
		BigDecimal remainingLimit = dailyTransferAmountLimit.subtract(money(dailyTotal));
		if (transferAmount.compareTo(remainingLimit) > 0) {
			throw limitExceeded("Daily transfer amount limit exceeded. Remaining limit is "
					+ currency + " " + remainingLimit.max(BigDecimal.ZERO).toPlainString());
		}
	}

	public TransferLimitSummaryResponse findSummary(Long ownerUserId) {
		Wallet wallet = walletRepository.findByOwnerUserId(ownerUserId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Wallet is not ready yet"));
		Instant dayStart = todayStart();
		Instant resetAt = nextDayStart(dayStart);
		BigDecimal usedAmount = dailyTransferAmount(ownerUserId, dayStart, resetAt);
		BigDecimal remainingAmount = dailyTransferAmountLimit.subtract(usedAmount).max(BigDecimal.ZERO);
		long usedCount = dailyTransferCount(ownerUserId, dayStart, resetAt);
		long remainingCount = Math.max(0, dailyTransferCountLimit - usedCount);

		return new TransferLimitSummaryResponse(
				wallet.getCurrency(),
				singleTransferLimit,
				dailyTransferAmountLimit,
				usedAmount,
				remainingAmount,
				dailyTransferCountLimit,
				usedCount,
				remainingCount,
				resetAt,
				remainingAmount.compareTo(BigDecimal.ZERO) <= 0,
				remainingCount <= 0
		);
	}

	private long dailyTransferCount(Long ownerUserId, Instant dayStart, Instant dayEnd) {
		return transactionRepository
				.countByWalletOwnerUserIdAndTypeAndStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
						ownerUserId,
						WalletTransactionType.TRANSFER_DEBIT,
						WalletTransactionStatus.COMPLETED,
						dayStart,
						dayEnd
				);
	}

	private BigDecimal dailyTransferAmount(Long ownerUserId, Instant dayStart, Instant dayEnd) {
		return money(transactionRepository.sumAmountByWalletOwnerUserIdAndTypeAndStatusAndCreatedAtBetween(
				ownerUserId,
				WalletTransactionType.TRANSFER_DEBIT,
				WalletTransactionStatus.COMPLETED,
				dayStart,
				dayEnd
		));
	}

	private Instant todayStart() {
		return LocalDate.now(clock).atStartOfDay().toInstant(ZoneOffset.UTC);
	}

	private Instant nextDayStart(Instant dayStart) {
		return dayStart.plusSeconds(86_400);
	}

	private ResponseStatusException limitExceeded(String message) {
		return new ResponseStatusException(HttpStatus.CONFLICT, message);
	}

	private BigDecimal money(BigDecimal amount) {
		if (amount == null) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		return amount.setScale(2, RoundingMode.HALF_UP);
	}
}
