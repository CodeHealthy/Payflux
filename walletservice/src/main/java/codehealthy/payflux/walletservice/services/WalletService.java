package codehealthy.payflux.walletservice.services;

import codehealthy.payflux.events.AdminWalletStatusChangedEvent;
import codehealthy.payflux.events.AccountCreatedEvent;
import codehealthy.payflux.events.TransferCompletedEvent;
import codehealthy.payflux.events.TransferOtpRequestedEvent;
import codehealthy.payflux.walletservice.dto.DepositRequest;
import codehealthy.payflux.walletservice.dto.ConfirmTransferRequest;
import codehealthy.payflux.walletservice.dto.AdminWalletStatusRequest;
import codehealthy.payflux.walletservice.dto.PendingTransfer;
import codehealthy.payflux.walletservice.dto.ReverseTransferRequest;
import codehealthy.payflux.walletservice.dto.TransferRequest;
import codehealthy.payflux.walletservice.dto.TransferConfirmationResponse;
import codehealthy.payflux.walletservice.dto.WalletDashboardResponse;
import codehealthy.payflux.walletservice.dto.WalletResponse;
import codehealthy.payflux.walletservice.dto.WalletTransactionResponse;
import codehealthy.payflux.walletservice.dto.WalletTransferActivityResponse;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
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
	private final TransferOtpResendLimitService transferOtpResendLimitService;
	private final TransferIdempotencyService transferIdempotencyService;
	private final WalletLedgerService walletLedgerService;

	public WalletService(
			WalletRepository walletRepository,
			WalletTransactionRepository transactionRepository,
			WalletTransferRepository transferRepository,
			OutboxService outboxService,
			TransferConfirmationService transferConfirmationService,
			TransferRateLimitService transferRateLimitService,
			TransferOtpResendLimitService transferOtpResendLimitService,
			TransferIdempotencyService transferIdempotencyService,
			WalletLedgerService walletLedgerService
	) {
		this.walletRepository = walletRepository;
		this.transactionRepository = transactionRepository;
		this.transferRepository = transferRepository;
		this.outboxService = outboxService;
		this.transferConfirmationService = transferConfirmationService;
		this.transferRateLimitService = transferRateLimitService;
		this.transferOtpResendLimitService = transferOtpResendLimitService;
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
		List<WalletTransferActivityResponse> transferActivities = transferRepository
				.findTop10ByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId)
				.stream()
				.map(WalletTransferActivityResponse::from)
				.toList();

		return new WalletDashboardResponse(WalletResponse.from(wallet), transactions, transferActivities);
	}

	@Transactional(readOnly = true)
	public List<WalletResponse> findAdminWallets() {
		return walletRepository.findTop100ByOrderByUpdatedAtDesc()
				.stream()
				.map(WalletResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<WalletTransferActivityResponse> findAdminTransferActivities() {
		return transferRepository.findTop100ByOrderByUpdatedAtDesc()
				.stream()
				.map(WalletTransferActivityResponse::from)
				.toList();
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

	@Transactional
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
		String requestHash = transferRequestHash(receiver.getAccountNumber(), amount, description);

		WalletTransfer transfer = transferRepository.findByOwnerUserIdAndIdempotencyKey(ownerUserId, idempotencyKey)
				.orElseGet(() -> transferRepository.save(new WalletTransfer(
						ownerUserId,
						reference,
						idempotencyKey,
						senderSnapshot.getAccountNumber(),
						receiver.getAccountNumber(),
						amount,
						senderSnapshot.getCurrency(),
						description,
						requestHash
				)));
		assertSameTransferRequest(transfer, requestHash);
		if (transfer.getStatus() == WalletTransferStatus.COMPLETED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Transfer is already completed");
		}

		PendingTransfer pendingTransfer = transferConfirmationService.pendingTransfer(
				ownerUserId,
				senderSnapshot.getEmail(),
				senderSnapshot.getAccountNumber(),
				receiver.getAccountNumber(),
				receiver.getFullName(),
				amount,
				senderSnapshot.getCurrency(),
				description,
				idempotencyKey
		);

		Instant resendAvailableAt = transferOtpResendLimitService.startInitialCooldown(ownerUserId, pendingTransfer.confirmationId());
		TransferConfirmationResponse confirmationResponse = transferConfirmationService.create(pendingTransfer, resendAvailableAt);
		enqueueTransferOtpRequested(pendingTransfer);
		return confirmationResponse;
	}

	@Transactional
	public TransferConfirmationResponse resendTransferOtp(Long ownerUserId, String confirmationId) {
		String requiredConfirmationId = requireText(confirmationId, "confirmationId");
		Instant resendAvailableAt = transferOtpResendLimitService.assertResendAllowed(ownerUserId, requiredConfirmationId);
		PendingTransfer pendingTransfer = transferConfirmationService.resend(ownerUserId, requiredConfirmationId);
		enqueueTransferOtpRequested(pendingTransfer);
		return transferConfirmationService.response(pendingTransfer, resendAvailableAt);
	}

	@Transactional
	public WalletDashboardResponse confirmTransfer(Long ownerUserId, ConfirmTransferRequest request) {
		String idempotencyKey = requireText(request.idempotencyKey(), "idempotencyKey");
		return transferIdempotencyService.findCompleted(ownerUserId, idempotencyKey)
				.orElseGet(() -> confirmTransferOnce(ownerUserId, request, idempotencyKey));
	}

	@Transactional
	public WalletDashboardResponse reverseTransfer(Long adminUserId, String transactionReference, ReverseTransferRequest request) {
		String reference = requireText(transactionReference, "transactionReference");
		WalletTransfer transfer = transferRepository.findByTransactionReferenceForUpdate(reference)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found"));

		if (transfer.getStatus() == WalletTransferStatus.REVERSED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Transfer is already reversed");
		}
		if (transfer.getStatus() != WalletTransferStatus.COMPLETED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only completed transfers can be reversed");
		}

		List<Wallet> lockedWallets = walletRepository.findByAccountNumberInForUpdate(
				List.of(transfer.getSenderAccountNumber(), transfer.getReceiverAccountNumber())
		);
		if (lockedWallets.size() != 2) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer wallet not found");
		}

		Wallet sender = lockedWallets.stream()
				.filter(wallet -> wallet.getAccountNumber().equals(transfer.getSenderAccountNumber()))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer sender wallet not found"));
		Wallet receiver = lockedWallets.stream()
				.filter(wallet -> wallet.getAccountNumber().equals(transfer.getReceiverAccountNumber()))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer receiver wallet not found"));

		assertActive(sender);
		assertActive(receiver);

		String reversalReason = optionalText(request == null ? null : request.reason(), "Transfer reversed by admin " + adminUserId);
		String reversalReference = "REV-" + reference;
		rejectDuplicateReference(reversalReference + "-D");

		walletLedgerService.debit(receiver, reversalReference + "-D", transfer.getAmount(), reversalReason);
		walletLedgerService.credit(sender, reversalReference + "-C", transfer.getAmount(), reversalReason);

		transactionRepository.save(new WalletTransaction(
				receiver,
				sender,
				reversalReference + "-D",
				WalletTransactionType.REVERSAL_DEBIT,
				transfer.getAmount(),
				receiver.getCurrency(),
				reversalReason,
				sender.getAccountNumber()
		));
		transactionRepository.save(new WalletTransaction(
				sender,
				receiver,
				reversalReference + "-C",
				WalletTransactionType.REVERSAL_CREDIT,
				transfer.getAmount(),
				sender.getCurrency(),
				reversalReason,
				receiver.getAccountNumber()
		));
		transfer.markReversed(reversalReason);

		return findDashboard(sender.getOwnerUserId());
	}

	@Transactional
	public WalletResponse suspendWallet(Long adminUserId, Long ownerUserId, AdminWalletStatusRequest request) {
		Wallet wallet = walletRepository.findByOwnerUserIdForUpdate(ownerUserId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet is not ready yet"));
		if (wallet.getStatus() == WalletStatus.CLOSED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Closed wallets cannot be suspended");
		}
		if (wallet.getStatus() == WalletStatus.SUSPENDED) {
			return WalletResponse.from(wallet);
		}

		WalletStatus previousStatus = wallet.getStatus();
		wallet.suspend();
		enqueueWalletStatusAudit(adminUserId, wallet, previousStatus, request, "Wallet suspended by admin");
		return WalletResponse.from(wallet);
	}

	@Transactional
	public WalletResponse activateWallet(Long adminUserId, Long ownerUserId, AdminWalletStatusRequest request) {
		Wallet wallet = walletRepository.findByOwnerUserIdForUpdate(ownerUserId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet is not ready yet"));
		if (wallet.getStatus() == WalletStatus.CLOSED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Closed wallets cannot be activated");
		}
		if (wallet.getStatus() == WalletStatus.ACTIVE) {
			return WalletResponse.from(wallet);
		}

		WalletStatus previousStatus = wallet.getStatus();
		wallet.activate();
		enqueueWalletStatusAudit(adminUserId, wallet, previousStatus, request, "Wallet activated by admin");
		return WalletResponse.from(wallet);
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

			PendingTransfer pendingTransfer = transferConfirmationService.consume(
					ownerUserId,
					request.confirmationId(),
					request.otp()
			);
			if (!idempotencyKey.equals(pendingTransfer.idempotencyKey())) {
				transfer.markFailed("Transfer confirmation does not match this request");
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer confirmation does not match this request");
			}
			transferOtpResendLimitService.clear(ownerUserId, pendingTransfer.confirmationId());
			assertSameTransferRequest(
					transfer,
					transferRequestHash(
							pendingTransfer.receiverAccountNumber(),
							pendingTransfer.amount(),
							optionalText(pendingTransfer.description(), "PayFlux transfer")
					)
			);
			transfer.markProcessing();

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

	private void enqueueWalletStatusAudit(
			Long adminUserId,
			Wallet wallet,
			WalletStatus previousStatus,
			AdminWalletStatusRequest request,
			String fallbackReason
	) {
		outboxService.enqueueAdminWalletStatusChanged(new AdminWalletStatusChangedEvent(
				UUID.randomUUID().toString(),
				adminUserId,
				wallet.getOwnerUserId(),
				wallet.getAccountNumber(),
				previousStatus.name(),
				wallet.getStatus().name(),
				optionalText(request == null ? null : request.reason(), fallbackReason),
				java.time.Instant.now()
		));
	}

	private void enqueueTransferOtpRequested(PendingTransfer pendingTransfer) {
		outboxService.enqueueTransferOtpRequested(new TransferOtpRequestedEvent(
				UUID.randomUUID().toString(),
				pendingTransfer.ownerUserId(),
				pendingTransfer.email(),
				pendingTransfer.receiverName(),
				pendingTransfer.receiverAccountNumber(),
				pendingTransfer.amount(),
				pendingTransfer.currency(),
				pendingTransfer.otp(),
				pendingTransfer.expiresAt(),
				Instant.now()
		));
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

	private void assertSameTransferRequest(WalletTransfer transfer, String requestHash) {
		if (!transfer.hasSameRequestHash(requestHash)) {
			throw new ResponseStatusException(
					HttpStatus.CONFLICT,
					"Idempotency key was already used for a different transfer request"
			);
		}
	}

	private String transferRequestHash(String receiverAccountNumber, BigDecimal amount, String description) {
		String canonicalRequest = requireText(receiverAccountNumber, "receiverAccountNumber")
				+ "|"
				+ requireMoney(amount).toPlainString()
				+ "|"
				+ optionalText(description, "PayFlux transfer");
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(canonicalRequest.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
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
