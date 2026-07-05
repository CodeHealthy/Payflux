package codehealthy.payflux.walletservice.controllers;

import codehealthy.payflux.walletservice.dto.DepositRequest;
import codehealthy.payflux.walletservice.dto.ConfirmTransferRequest;
import codehealthy.payflux.walletservice.dto.TransferRequest;
import codehealthy.payflux.walletservice.dto.TransferConfirmationResponse;
import codehealthy.payflux.walletservice.dto.WalletDashboardResponse;
import codehealthy.payflux.walletservice.services.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/wallets")
public class WalletController {

	private final WalletService walletService;

	public WalletController(WalletService walletService) {
		this.walletService = walletService;
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

	@PostMapping("/transfers/confirmations")
	public TransferConfirmationResponse prepareTransfer(@AuthenticationPrincipal Jwt jwt, @RequestBody TransferRequest request) {
		return walletService.prepareTransfer(currentUserId(jwt), request);
	}

	@PostMapping("/transfers/confirm")
	public WalletDashboardResponse confirmTransfer(@AuthenticationPrincipal Jwt jwt, @RequestBody ConfirmTransferRequest request) {
		return walletService.confirmTransfer(currentUserId(jwt), request);
	}

	private Long currentUserId(Jwt jwt) {
		Number userId = jwt.getClaim("userId");
		if (userId == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is missing userId");
		}

		return userId.longValue();
	}
}
