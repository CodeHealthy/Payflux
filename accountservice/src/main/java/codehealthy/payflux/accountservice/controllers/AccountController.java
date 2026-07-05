package codehealthy.payflux.accountservice.controllers;

import codehealthy.payflux.accountservice.dto.AccountLookupResponse;
import codehealthy.payflux.accountservice.dto.AccountResponse;
import codehealthy.payflux.accountservice.services.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@GetMapping
	public List<AccountResponse> findCurrentUserAccounts(@AuthenticationPrincipal Jwt jwt) {
		return accountService.findAccountsForUser(currentUserId(jwt));
	}

	@GetMapping("/lookup/{accountNumber}")
	public AccountLookupResponse findAccountByAccountNumber(@PathVariable String accountNumber) {
		return accountService.findAccountByAccountNumber(accountNumber);
	}

	private Long currentUserId(Jwt jwt) {
		Number userId = jwt.getClaim("userId");
		if (userId == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is missing userId");
		}

		return userId.longValue();
	}
}
