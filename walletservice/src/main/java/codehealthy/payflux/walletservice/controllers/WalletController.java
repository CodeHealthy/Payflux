package codehealthy.payflux.walletservice.controllers;

import codehealthy.payflux.walletservice.dto.AdminWalletStatusRequest;
import codehealthy.payflux.walletservice.dto.CreateTransferDisputeRequest;
import codehealthy.payflux.walletservice.dto.DepositRequest;
import codehealthy.payflux.walletservice.dto.ConfirmTransferRequest;
import codehealthy.payflux.walletservice.dto.ReverseTransferRequest;
import codehealthy.payflux.walletservice.dto.ResolveTransferDisputeRequest;
import codehealthy.payflux.walletservice.dto.TransferRequest;
import codehealthy.payflux.walletservice.dto.TransferLimitSummaryResponse;
import codehealthy.payflux.walletservice.dto.TransferConfirmationResponse;
import codehealthy.payflux.walletservice.dto.WalletDashboardResponse;
import codehealthy.payflux.walletservice.dto.WalletResponse;
import codehealthy.payflux.walletservice.dto.WalletTransferActivityResponse;
import codehealthy.payflux.walletservice.dto.WalletTransferDisputeResponse;
import codehealthy.payflux.walletservice.services.TransferLimitService;
import codehealthy.payflux.walletservice.services.WalletService;
import codehealthy.payflux.walletservice.services.WalletStatementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/wallets")
public class WalletController {

	private final WalletService walletService;
	private final WalletStatementService walletStatementService;
	private final TransferLimitService transferLimitService;

	public WalletController(
			WalletService walletService,
			WalletStatementService walletStatementService,
			TransferLimitService transferLimitService
	) {
		this.walletService = walletService;
		this.walletStatementService = walletStatementService;
		this.transferLimitService = transferLimitService;
	}

	@GetMapping("/me")
	public WalletDashboardResponse findCurrentUserWallet(@AuthenticationPrincipal Jwt jwt) {
		return walletService.findDashboard(currentUserId(jwt));
	}

	@PostMapping("/deposits")
	public WalletDashboardResponse deposit(@AuthenticationPrincipal Jwt jwt, @RequestBody DepositRequest request) {
		return walletService.deposit(currentUserId(jwt), request);
	}

	@PostMapping("/transfers")
	public WalletDashboardResponse transfer(@AuthenticationPrincipal Jwt jwt, @RequestBody TransferRequest request) {
		return walletService.transfer(currentUserId(jwt), request);
	}

	@GetMapping("/transfer-limits")
	public TransferLimitSummaryResponse findTransferLimits(@AuthenticationPrincipal Jwt jwt) {
		return transferLimitService.findSummary(currentUserId(jwt));
	}

	@PostMapping("/transfers/confirmations")
	public TransferConfirmationResponse prepareTransfer(@AuthenticationPrincipal Jwt jwt, @RequestBody TransferRequest request) {
		return walletService.prepareTransfer(currentUserId(jwt), request);
	}

	@PostMapping("/transfers/confirmations/{confirmationId}/resend")
	public TransferConfirmationResponse resendTransferOtp(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable String confirmationId
	) {
		return walletService.resendTransferOtp(currentUserId(jwt), confirmationId);
	}

	@PostMapping("/transfers/confirm")
	public WalletDashboardResponse confirmTransfer(@AuthenticationPrincipal Jwt jwt, @RequestBody ConfirmTransferRequest request) {
		return walletService.confirmTransfer(currentUserId(jwt), request);
	}

	@GetMapping("/transfers/disputes")
	public List<WalletTransferDisputeResponse> findDisputes(@AuthenticationPrincipal Jwt jwt) {
		return walletService.findDisputes(currentUserId(jwt));
	}

	@PostMapping("/transfers/{transactionReference}/disputes")
	public WalletTransferDisputeResponse openDispute(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable String transactionReference,
			@RequestBody CreateTransferDisputeRequest request
	) {
		return walletService.openDispute(currentUserId(jwt), transactionReference, request);
	}

	@GetMapping("/statements/export")
	public ResponseEntity<String> exportStatement(
			@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
	) {
		String csv = walletStatementService.exportCsv(currentUserId(jwt), from, to);
		String filename = "payflux-statement-" + (from == null ? "latest" : from) + "-to-" + (to == null ? "today" : to) + ".csv";

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
				.contentType(MediaType.parseMediaType("text/csv"))
				.body(csv);
	}

	@PostMapping("/admin/transfers/{transactionReference}/reverse")
	public WalletDashboardResponse reverseTransfer(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable String transactionReference,
			@RequestBody ReverseTransferRequest request
	) {
		return walletService.reverseTransfer(currentUserId(jwt), transactionReference, request);
	}

	@GetMapping("/admin")
	public List<WalletResponse> findAdminWallets() {
		return walletService.findAdminWallets();
	}

	@GetMapping("/admin/transfers")
	public List<WalletTransferActivityResponse> findAdminTransferActivities() {
		return walletService.findAdminTransferActivities();
	}

	@GetMapping("/admin/disputes")
	public List<WalletTransferDisputeResponse> findAdminDisputes() {
		return walletService.findAdminDisputes();
	}

	@PostMapping("/admin/disputes/{disputeId}/review")
	public WalletTransferDisputeResponse markDisputeUnderReview(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long disputeId
	) {
		return walletService.markDisputeUnderReview(currentUserId(jwt), disputeId);
	}

	@PostMapping("/admin/disputes/{disputeId}/reject")
	public WalletTransferDisputeResponse rejectDispute(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long disputeId,
			@RequestBody(required = false) ResolveTransferDisputeRequest request
	) {
		return walletService.rejectDispute(currentUserId(jwt), disputeId, request);
	}

	@PostMapping("/admin/disputes/{disputeId}/resolve")
	public WalletTransferDisputeResponse resolveDispute(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long disputeId,
			@RequestBody(required = false) ResolveTransferDisputeRequest request
	) {
		return walletService.resolveDisputeWithReversal(currentUserId(jwt), disputeId, request);
	}

	@PostMapping("/admin/users/{ownerUserId}/suspend")
	public WalletResponse suspendWallet(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long ownerUserId,
			@RequestBody(required = false) AdminWalletStatusRequest request
	) {
		return walletService.suspendWallet(currentUserId(jwt), ownerUserId, request);
	}

	@PostMapping("/admin/users/{ownerUserId}/activate")
	public WalletResponse activateWallet(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long ownerUserId,
			@RequestBody(required = false) AdminWalletStatusRequest request
	) {
		return walletService.activateWallet(currentUserId(jwt), ownerUserId, request);
	}

	private Long currentUserId(Jwt jwt) {
		Number userId = jwt.getClaim("userId");
		if (userId == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is missing userId");
		}

		return userId.longValue();
	}
}
