package codehealthy.payflux.beneficiaryservice.services;

import codehealthy.payflux.beneficiaryservice.clients.AccountLookupResponse;
import codehealthy.payflux.beneficiaryservice.clients.AccountServiceClient;
import codehealthy.payflux.beneficiaryservice.dto.BeneficiaryResponse;
import codehealthy.payflux.beneficiaryservice.dto.CreateBeneficiaryRequest;
import codehealthy.payflux.beneficiaryservice.dto.RecipientVerificationResponse;
import codehealthy.payflux.beneficiaryservice.events.BeneficiaryAddedEvent;
import codehealthy.payflux.beneficiaryservice.models.Beneficiary;
import codehealthy.payflux.beneficiaryservice.models.BeneficiaryStatus;
import codehealthy.payflux.beneficiaryservice.repositories.BeneficiaryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BeneficiaryService {

	private final BeneficiaryRepository beneficiaryRepository;
	private final BeneficiaryEventPublisher beneficiaryEventPublisher;
	private final AccountServiceClient accountServiceClient;

	public BeneficiaryService(
			BeneficiaryRepository beneficiaryRepository,
			BeneficiaryEventPublisher beneficiaryEventPublisher,
			AccountServiceClient accountServiceClient
	) {
		this.beneficiaryRepository = beneficiaryRepository;
		this.beneficiaryEventPublisher = beneficiaryEventPublisher;
		this.accountServiceClient = accountServiceClient;
	}

	@Transactional
	public BeneficiaryResponse addBeneficiary(Long ownerUserId, String bearerToken, CreateBeneficiaryRequest request) {
		String accountNumber = request.beneficiaryAccountNumber().trim();
		if (beneficiaryRepository.existsByOwnerUserIdAndBeneficiaryAccountNumber(ownerUserId, accountNumber)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Beneficiary already exists");
		}

		AccountLookupResponse account = accountServiceClient.findByAccountNumber(accountNumber, bearerToken);
		if (ownerUserId.equals(account.ownerUserId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot add your own account as a beneficiary");
		}

		Beneficiary beneficiary = beneficiaryRepository.save(new Beneficiary(
				ownerUserId,
				account.accountNumber(),
				request.nickname().trim(),
				account.fullName()
		));

		beneficiaryEventPublisher.publishBeneficiaryAdded(new BeneficiaryAddedEvent(
				beneficiary.getId(),
				beneficiary.getOwnerUserId(),
				beneficiary.getBeneficiaryAccountNumber(),
				beneficiary.getNickname(),
				beneficiary.getCreatedAt()
		));

		return BeneficiaryResponse.from(beneficiary);
	}

	public List<BeneficiaryResponse> findCurrentUserBeneficiaries(Long ownerUserId) {
		return beneficiaryRepository.findByOwnerUserIdAndStatusOrderByCreatedAtDesc(ownerUserId, BeneficiaryStatus.ACTIVE)
				.stream()
				.map(BeneficiaryResponse::from)
				.toList();
	}

	public BeneficiaryResponse findCurrentUserBeneficiary(Long ownerUserId, Long beneficiaryId) {
		return beneficiaryRepository.findByIdAndOwnerUserId(beneficiaryId, ownerUserId)
				.map(BeneficiaryResponse::from)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beneficiary not found"));
	}

	public RecipientVerificationResponse verifyRecipient(Long ownerUserId, String bearerToken, String accountNumber) {
		AccountLookupResponse account = accountServiceClient.findByAccountNumber(requireAccountNumber(accountNumber), bearerToken);
		if (ownerUserId.equals(account.ownerUserId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot transfer to your own account");
		}

		boolean savedBeneficiary = beneficiaryRepository.existsByOwnerUserIdAndBeneficiaryAccountNumber(
				ownerUserId,
				account.accountNumber()
		);

		return new RecipientVerificationResponse(
				account.accountNumber(),
				safeDisplayName(account.fullName()),
				"PayFlux",
				savedBeneficiary
		);
	}

	private String requireAccountNumber(String accountNumber) {
		if (accountNumber == null || accountNumber.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recipient account number is required");
		}

		return accountNumber.trim();
	}

	private String safeDisplayName(String fullName) {
		if (fullName == null || fullName.isBlank()) {
			return "PayFlux customer";
		}

		String[] parts = fullName.trim().split("\\s+");
		if (parts.length == 1) {
			return parts[0];
		}

		return parts[0] + " " + parts[parts.length - 1].charAt(0) + ".";
	}
}
