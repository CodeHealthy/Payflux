package codehealthy.payflux.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSecurityQuestionRequest(
		@NotBlank String currentPassword,
		@NotBlank @Size(min = 12, max = 160) String securityQuestion,
		@NotBlank @Size(min = 3, max = 120) String securityAnswer
) {
}
