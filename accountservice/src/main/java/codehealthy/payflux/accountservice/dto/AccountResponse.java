package codehealthy.payflux.accountservice.dto;

import codehealthy.payflux.accountservice.models.Account;

import java.time.Instant;

public record AccountResponse(
		Long id,
		Long ownerUserId,
		String accountNumber,
		String fullName,
		String email,
		Instant createdAt
) {
	public static AccountResponse from(Account account) {
		return new AccountResponse(
				account.getId(),
				account.getOwnerUserId(),
				account.getAccountNumber(),
				account.getFullName(),
				account.getEmail(),
				account.getCreatedAt()
		);
	}
}
