package codehealthy.payflux.accountservice.services;

import codehealthy.payflux.accountservice.repositories.AccountRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {

	private static final String BANK_PREFIX = "9201";
	private static final int RANDOM_DIGITS = 8;
	private static final int MAX_ATTEMPTS = 20;

	private final SecureRandom secureRandom = new SecureRandom();
	private final AccountRepository accountRepository;

	public AccountNumberGenerator(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	public String generateUniqueAccountNumber() {
		for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
			String accountNumber = BANK_PREFIX + paddedRandomDigits();
			if (!accountRepository.existsByAccountNumber(accountNumber)) {
				return accountNumber;
			}
		}

		throw new IllegalStateException("Unable to generate a unique account number");
	}

	private String paddedRandomDigits() {
		int upperBound = (int) Math.pow(10, RANDOM_DIGITS);
		return String.format("%0" + RANDOM_DIGITS + "d", secureRandom.nextInt(upperBound));
	}
}
