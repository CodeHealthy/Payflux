package codehealthy.payflux.beneficiaryservice.controllers;

import codehealthy.payflux.beneficiaryservice.dto.BeneficiaryResponse;
import codehealthy.payflux.beneficiaryservice.dto.CreateBeneficiaryRequest;
import codehealthy.payflux.beneficiaryservice.services.BeneficiaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/beneficiaries")
public class BeneficiaryController {

	private final BeneficiaryService beneficiaryService;

	public BeneficiaryController(BeneficiaryService beneficiaryService) {
		this.beneficiaryService = beneficiaryService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BeneficiaryResponse addBeneficiary(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody CreateBeneficiaryRequest request
	) {
		return beneficiaryService.addBeneficiary(currentUserId(jwt), jwt.getTokenValue(), request);
	}

	@GetMapping
	public List<BeneficiaryResponse> findCurrentUserBeneficiaries(@AuthenticationPrincipal Jwt jwt) {
		return beneficiaryService.findCurrentUserBeneficiaries(currentUserId(jwt));
	}

	@GetMapping("/{beneficiaryId}")
	public BeneficiaryResponse findCurrentUserBeneficiary(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long beneficiaryId
	) {
		return beneficiaryService.findCurrentUserBeneficiary(currentUserId(jwt), beneficiaryId);
	}

	private Long currentUserId(Jwt jwt) {
		Number userId = jwt.getClaim("userId");
		if (userId == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is missing userId");
		}

		return userId.longValue();
	}
}
