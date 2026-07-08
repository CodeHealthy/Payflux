package codehealthy.payflux.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
		@NotBlank @Email String email,
		@Size(min = 3, max = 120) String securityAnswer,
		@Size(min = 6, max = 6) String resetCode,
		@NotBlank @Size(min = 8) String newPassword
) {
}
