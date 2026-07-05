package codehealthy.payflux.accountservice.services;

import codehealthy.payflux.accountservice.dto.AccountResponse;
import codehealthy.payflux.accountservice.dto.AccountLookupResponse;
import codehealthy.payflux.accountservice.models.Account;
import codehealthy.payflux.accountservice.repositories.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AccountService {

	private final AccountRepository accountRepository;
	private final AccountEventPublisher accountEventPublisher;
	private final AccountNumberGenerator accountNumberGenerator;

	public AccountService(
			AccountRepository accountRepository,
			AccountEventPublisher accountEventPublisher,
			AccountNumberGenerator accountNumberGenerator
	) {
		this.accountRepository = accountRepository;
		this.accountEventPublisher = accountEventPublisher;
		this.accountNumberGenerator = accountNumberGenerator;
	}

	@Transactional
	public AccountResponse createAccountForRegisteredUser(Long ownerUserId, String fullName, String email) {
		if (accountRepository.existsByOwnerUserId(ownerUserId)) {
			return accountRepository.findByOwnerUserIdOrderByCreatedAtDesc(ownerUserId)
					.stream()
					.findFirst()
					.map(AccountResponse::from)
					.orElseThrow(() -> new IllegalStateException("Existing account could not be loaded"));
		}

		return createAccountRecord(
				ownerUserId,
				requireText(fullName, "fullName"),
				requireText(email, "email").toLowerCase()
		);
	}

	private AccountResponse createAccountRecord(Long ownerUserId, String fullName, String email) {
		String accountNumber = accountNumberGenerator.generateUniqueAccountNumber();
		Account account = accountRepository.save(new Account(ownerUserId, accountNumber, fullName, email));
		accountEventPublisher.publishAccountCreated(account);

		return AccountResponse.from(account);
	}

	@Transactional(readOnly = true)
	public List<AccountResponse> findAccountsForUser(Long ownerUserId) {
		return accountRepository.findByOwnerUserIdOrderByCreatedAtDesc(ownerUserId)
				.stream()
				.map(AccountResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public AccountLookupResponse findAccountByAccountNumber(String accountNumber) {
		return accountRepository.findByAccountNumber(requireText(accountNumber, "accountNumber"))
				.map(AccountLookupResponse::from)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account number not found"));
	}

	private String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}

		return value.trim();
	}
}
