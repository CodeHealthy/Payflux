package codehealthy.payflux.authservice.dto;

public record RegistrationResponse(
		String email,
		boolean verificationRequired,
		String message
) {
	public static RegistrationResponse verificationRequired(String email) {
		return new RegistrationResponse(
				email,
				true,
				"Profile created. Verify your email before signing in."
		);
	}
}
