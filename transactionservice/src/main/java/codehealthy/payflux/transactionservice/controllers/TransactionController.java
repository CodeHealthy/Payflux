package codehealthy.payflux.transactionservice.controllers;

import codehealthy.payflux.transactionservice.dto.TransactionResponse;
import codehealthy.payflux.transactionservice.services.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@GetMapping
	public List<TransactionResponse> findCurrentUserTransactions(@AuthenticationPrincipal Jwt jwt) {
		return transactionService.findCurrentUserTransactions(currentUserId(jwt));
	}

	@GetMapping("/{transactionReference}")
	public TransactionResponse findTransactionDetails(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable String transactionReference
	) {
		return transactionService.findTransactionDetails(transactionReference, currentUserId(jwt), isAdmin(jwt));
	}

	private Long currentUserId(Jwt jwt) {
		Number userId = jwt.getClaim("userId");
		if (userId == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is missing userId");
		}

		return userId.longValue();
	}

	private boolean isAdmin(Jwt jwt) {
		return "ADMIN".equals(jwt.getClaimAsString("role"));
	}
}
