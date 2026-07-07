package codehealthy.payflux.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
		@NotBlank String fullName
) {
}
