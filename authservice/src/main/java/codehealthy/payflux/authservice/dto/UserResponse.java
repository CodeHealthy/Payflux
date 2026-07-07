package codehealthy.payflux.authservice.dto;

import codehealthy.payflux.authservice.models.AppUser;
import codehealthy.payflux.authservice.models.UserRole;

import java.time.Instant;

public record UserResponse(
		Long id,
		String fullName,
		String email,
		UserRole role,
		Instant createdAt,
		boolean securityQuestionConfigured
) {
	public static UserResponse from(AppUser user) {
		return new UserResponse(
				user.getId(),
				user.getFullName(),
				user.getEmail(),
				user.getRole(),
				user.getCreatedAt(),
				user.hasSecurityQuestion()
		);
	}
}
