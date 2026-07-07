package codehealthy.payflux.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
		@NotBlank @Email String email,
		@NotBlank @Size(min = 3, max = 120) String securityAnswer,
		@NotBlank @Size(min = 8) String newPassword
) {
}
