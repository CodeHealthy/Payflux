package codehealthy.payflux.beneficiaryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBeneficiaryRequest(
		@NotBlank
		@Size(max = 32)
		String beneficiaryAccountNumber,

		@NotBlank
		@Size(max = 100)
		String nickname
) {
}
