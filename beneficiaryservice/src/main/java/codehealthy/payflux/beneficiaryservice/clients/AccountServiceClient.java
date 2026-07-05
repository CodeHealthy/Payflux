package codehealthy.payflux.beneficiaryservice.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AccountServiceClient {

	private final RestClient restClient;

	public AccountServiceClient(
			@Value("${app.services.account-service.base-url}") String accountServiceBaseUrl
	) {
		this.restClient = RestClient.builder()
				.baseUrl(accountServiceBaseUrl)
				.build();
	}

	public AccountLookupResponse findByAccountNumber(String accountNumber, String bearerToken) {
		try {
			AccountLookupResponse account = restClient.get()
					.uri("/accounts/lookup/{accountNumber}", accountNumber)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.retrieve()
					.body(AccountLookupResponse.class);

			if (account == null) {
				throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Account lookup returned an empty response");
			}

			return account;
		} catch (HttpClientErrorException.NotFound exception) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Beneficiary account number not found");
		} catch (HttpClientErrorException.Unauthorized exception) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account lookup authorization failed");
		} catch (RestClientException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Account lookup service is unavailable");
		}
	}
}
