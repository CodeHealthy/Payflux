package codehealthy.payflux.authservice.dto;

public record PasswordRecoveryQuestionResponse(
		String email,
		String securityQuestion
) {
}
