package codehealthy.payflux.walletservice.services;

import codehealthy.payflux.events.AccountCreatedEvent;
import codehealthy.payflux.events.TransferCompletedEvent;
import codehealthy.payflux.walletservice.dto.DepositRequest;
import codehealthy.payflux.walletservice.dto.ConfirmTransferRequest;
import codehealthy.payflux.walletservice.dto.PendingTransfer;
import codehealthy.payflux.walletservice.dto.TransferRequest;
import codehealthy.payflux.walletservice.dto.TransferConfirmationResponse;
import codehealthy.payflux.walletservice.dto.WalletDashboardResponse;
import codehealthy.payflux.walletservice.dto.WalletResponse;
import codehealthy.payflux.walletservice.dto.WalletTransactionResponse;
import codehealthy.payflux.walletservice.models.Wallet;
import codehealthy.payflux.walletservice.models.WalletStatus;
import codehealthy.payflux.walletservice.models.WalletTransaction;
import codehealthy.payflux.walletservice.models.WalletTransactionType;
import codehealthy.payflux.walletservice.models.WalletTransfer;
import codehealthy.payflux.walletservice.models.WalletTransferStatus;
import codehealthy.payflux.walletservice.repositories.WalletRepository;
import codehealthy.payflux.walletservice.repositories.WalletTransactionRepository;
import codehealthy.payflux.walletservice.repositories.WalletTransferRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

	private static final String DEFAULT_CURRENCY = "PKR";

	private final WalletRepository walletRepository;
	private final WalletTransactionRepository transactionRepository;
	private final WalletTransferRepository transferRepository;
	private final OutboxService outboxService;
	private final TransferConfirmationService transferConfirmationService;
	private final TransferRateLimitService transferRateLimitService;
	private final TransferIdempotencyService transferIdempotencyService;
	private final WalletLedgerService walletLedgerService;

	public WalletService(
			WalletRepository walletRepository,
			WalletTransactionRepository transactionRepository,
			WalletTransferRepository transferRepository,
			OutboxService outboxService,
			TransferConfirmationService transferConfirmationService,
			TransferRateLimitService transferRateLimitService,
			TransferIdempotencyService transferIdempotencyService,
			WalletLedgerService walletLedgerService
	) {
		this.walletRepository = walletRepository;
		this.transactionRepository = transactionRepository;
		this.transferRepository = transferRepository;
		this.outboxService = outboxService;
		this.transferConfirmationService = transferConfirmationService;
		this.transferRateLimitService = transferRateLimitService;
		this.transferIdempotencyService = transferIdempotencyService;
		this.walletLedgerService = walletLedgerService;
	}

	@Transactional
	public WalletResponse provisionWallet(AccountCreatedEvent event) {
		return walletRepository.findByOwnerUserId(event.ownerUserId())
				.map(WalletResponse::from)
				.orElseGet(() -> WalletResponse.from(walletRepository.save(new Wallet(
						event.ownerUserId(),
						event.accountId(),
						requireText(event.accountNumber(), "accountNumber"),
						requireText(event.fullName(), "fullName"),
						requireText(event.email(), "email").toLowerCase(),
						DEFAULT_CURRENCY
				))));
	}

	@Transactional(readOnly = true)
	public WalletDashboardResponse findDashboard(Long ownerUserId) {
		Wallet wallet = walletRepository.findByOwnerUserId(ownerUserId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet is not ready yet"));

		List<WalletTransactionResponse> transactions = transactionRepository
				.findTop20ByWalletOwnerUserIdOrderByCreatedAtDesc(ownerUserId)
				.stream()
				.map(WalletTransactionResponse::from)
				.toList();

		return new WalletDashboardResponse(WalletResponse.from(wallet), transactions);
	}

	@Transactional
	public WalletDashboardResponse deposit(Long ownerUserId, DepositRequest request) {
		Wallet wallet = walletRepository.findByOwnerUserIdForUpdate(ownerUserId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet is not ready yet"));
		assertActive(wallet);

		BigDecimal amount = requireMoney(request.amount());
		String reference = reference("DEP", request.idempotencyKey());
		rejectDuplicateReference(reference);

		walletLedgerService.credit(wallet, reference, amount, "Test funding deposit");
		transactionRepository.save(new WalletTransaction(
				wallet,
				null,
				reference,
				WalletTransactionType.DEPOSIT,
				amount,
				wallet.getCurrency(),
				"Test funding deposit",
				null
		));

		return findDashboard(ownerUserId);
	}

	@Transactional
	public WalletDashboardResponse transfer(Long ownerUserId, TransferRequest request) {
		return executeTransfer(ownerUserId, request);
	}

	@Transactional(readOnly = true)
	public TransferConfirmationResponse prepareTransfer(Long ownerUserId, TransferRequest request) {
		transferRateLimitService.assertAllowed(ownerUserId);

		Wallet senderSnapshot = walletRepository.findByOwnerUserId(ownerUserId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet is not ready yet"));
		assertActive(senderSnapshot);

		String receiverAccountNumber = requireText(request.receiverAccountNumber(), "receiverAccountNumber");
		if (senderSnapshot.getAccountNumber().equals(receiverAccountNumber)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot transfer to your own account");
		}

		Wallet receiver = walletRepository.findByAccountNumber(receiverAccountNumber)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver account is not a PayFlux wallet"));
		assertActive(receiver);

		BigDecimal amount = requireMoney(request.amount());
		if (senderSnapshot.getAvailableBalance().compareTo(amount) < 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient wallet balance");
		}
		String idempotencyKey = requireText(request.idempotencyKey(), "idempotencyKey");
		String reference = reference("TRF", idempotencyKey);
		String description = optionalText(request.description(), "PayFlux transfer");

		WalletTransfer transfer = transferRepository.findByOwnerUserIdAndIdempotencyKey(ownerUserId, idempotencyKey)
				.orElseGet(() -> transferRepository.save(new WalletTransfer(
						ownerUserId,
						reference,
						idempotencyKey,
						senderSnapshot.getAccountNumber(),
						receiver.getAccountNumber(),
						amount,
						senderSnapshot.getCurrency(),
						description
				)));
		if (transfer.getStatus() == WalletTransferStatus.COMPLETED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Transfer is already completed");
		}

		PendingTransfer pendingTransfer = transferConfirmationService.pendingTransfer(
				ownerUserId,
				senderSnapshot.getAccountNumber(),
				receiver.getAccountNumber(),
				receiver.getFullName(),
				amount,
				senderSnapshot.getCurrency(),
				description,
				idempotencyKey
		);

		return transferConfirmationService.create(pendingTransfer);
	}

	@Transactional
	public WalletDashboardResponse confirmTransfer(Long ownerUserId, ConfirmTransferRequest request) {
		String idempotencyKey = requireText(request.idempotencyKey(), "idempotencyKey");
		return transferIdempotencyService.findCompleted(ownerUserId, idempotencyKey)
				.orElseGet(() -> confirmTransferOnce(ownerUserId, request, idempotencyKey));
	}

	private WalletDashboardResponse confirmTransferOnce(
			Long ownerUserId,
			ConfirmTransferRequest request,
			String idempotencyKey
	) {
		String reference = reference("TRF", idempotencyKey);
		if (transactionRepository.existsByTransactionReference(reference + "-D")) {
			WalletDashboardResponse dashboard = findDashboard(ownerUserId);
			transferIdempotencyService.complete(ownerUserId, idempotencyKey, dashboard);
			return dashboard;
		}

		transferIdempotencyService.claim(ownerUserId, idempotencyKey);
		try {
			WalletTransfer transfer = transferRepository.findByOwnerUserIdAndIdempotencyKeyForUpdate(ownerUserId, idempotencyKey)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer confirmation state not found"));
			if (transfer.getStatus() == WalletTransferStatus.COMPLETED) {
				WalletDashboardResponse dashboard = findDashboard(ownerUserId);
				transferIdempotencyService.complete(ownerUserId, idempotencyKey, dashboard);
				return dashboard;
			}
			if (transfer.getStatus() == WalletTransferStatus.PROCESSING) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Transfer is already being processed. Please refresh shortly.");
			}
			transfer.markProcessing();

			PendingTransfer pendingTransfer = transferConfirmationService.consume(
					ownerUserId,
					request.confirmationId(),
					request.otp()
			);
			if (!idempotencyKey.equals(pendingTransfer.idempotencyKey())) {
				transfer.markFailed("Transfer confirmation does not match this request");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer confirmation does not match this request");
			}

			WalletDashboardResponse dashboard = executeTransfer(ownerUserId, new TransferRequest(
					pendingTransfer.receiverAccountNumber(),
					pendingTransfer.amount(),
					pendingTransfer.description(),
					pendingTransfer.idempotencyKey()
			));
			transfer.markCompleted();
			transferIdempotencyService.complete(ownerUserId, idempotencyKey, dashboard);
			return dashboard;
		} catch (RuntimeException ex) {
			transferRepository.findByOwnerUserIdAndIdempotencyKeyForUpdate(ownerUserId, idempotencyKey)
					.ifPresent(transfer -> {
						if (transfer.getStatus() == WalletTransferStatus.PROCESSING) {
							transfer.markFailed(statusMessage(ex));
						}
					});
			transferIdempotencyService.release(ownerUserId, idempotencyKey);
			throw ex;
		}
	}

	private WalletDashboardResponse executeTransfer(Long ownerUserId, TransferRequest request) {
		Wallet senderSnapshot = walletRepository.findByOwnerUserId(ownerUserId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet is not ready yet"));

		String receiverAccountNumber = requireText(request.receiverAccountNumber(), "receiverAccountNumber");
		if (senderSnapshot.getAccountNumber().equals(receiverAccountNumber)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot transfer to your own account");
		}

		List<Wallet> lockedWallets = walletRepository.findByAccountNumberInForUpdate(
				List.of(senderSnapshot.getAccountNumber(), receiverAccountNumber)
		);
		if (lockedWallets.size() != 2) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver account is not a PayFlux wallet");
		}

		Wallet sender = lockedWallets.stream()
				.filter(wallet -> wallet.getOwnerUserId().equals(ownerUserId))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet is not ready yet"));
		Wallet receiver = lockedWallets.stream()
				.filter(wallet -> wallet.getAccountNumber().equals(receiverAccountNumber))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver account is not a PayFlux wallet"));

		assertActive(sender);
		assertActive(receiver);

		BigDecimal amount = requireMoney(request.amount());
		if (sender.getAvailableBalance().compareTo(amount) < 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient wallet balance");
		}

		String reference = reference("TRF", request.idempotencyKey());
		rejectDuplicateReference(reference + "-D");

		String description = optionalText(request.description(), "PayFlux transfer");
		walletLedgerService.debit(sender, reference + "-D", amount, description);
		walletLedgerService.credit(receiver, reference + "-C", amount, description);

		transactionRepository.save(new WalletTransaction(
				sender,
				receiver,
				reference + "-D",
				WalletTransactionType.TRANSFER_DEBIT,
				amount,
				sender.getCurrency(),
				description,
				receiver.getAccountNumber()
		));
		transactionRepository.save(new WalletTransaction(
				receiver,
				sender,
				reference + "-C",
				WalletTransactionType.TRANSFER_CREDIT,
				amount,
				receiver.getCurrency(),
				description,
				sender.getAccountNumber()
		));
		outboxService.enqueueTransferCompleted(new TransferCompletedEvent(
				UUID.randomUUID().toString(),
				reference,
				sender.getOwnerUserId(),
				receiver.getOwnerUserId(),
				sender.getAccountNumber(),
				receiver.getAccountNumber(),
				amount,
				sender.getCurrency(),
				description,
				java.time.Instant.now()
		));

		return findDashboard(ownerUserId);
	}

	private void assertActive(Wallet wallet) {
		if (wallet.getStatus() != WalletStatus.ACTIVE) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Wallet is not active");
		}
	}

	private BigDecimal requireMoney(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
		}

		return amount.setScale(2, RoundingMode.HALF_UP);
	}

	private void rejectDuplicateReference(String reference) {
		if (transactionRepository.existsByTransactionReference(reference)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate wallet operation");
		}
	}

	private String reference(String prefix, String idempotencyKey) {
		String suffix = optionalText(idempotencyKey, UUID.randomUUID().toString());
		return prefix + "-" + suffix.replaceAll("[^A-Za-z0-9-]", "").toUpperCase();
	}

	private String optionalText(String value, String fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}

		return value.trim();
	}

	private String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
		}

		return value.trim();
	}

	private String statusMessage(RuntimeException exception) {
		if (exception instanceof ResponseStatusException responseStatusException
				&& responseStatusException.getReason() != null
				&& !responseStatusException.getReason().isBlank()) {
			return responseStatusException.getReason();
		}

		return "Transfer failed before completion";
	}
}
