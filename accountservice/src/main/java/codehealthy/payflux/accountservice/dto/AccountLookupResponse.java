package codehealthy.payflux.accountservice.dto;

import codehealthy.payflux.accountservice.models.Account;

public record AccountLookupResponse(
		Long ownerUserId,
		String accountNumber,
		String fullName
) {
	public static AccountLookupResponse from(Account account) {
		return new AccountLookupResponse(
				account.getOwnerUserId(),
				account.getAccountNumber(),
				account.getFullName()
		);
	}
}
