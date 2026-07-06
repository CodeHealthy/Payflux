package codehealthy.payflux.authservice.dto;

import java.time.Instant;

public record AuthResponse(
		String accessToken,
		String refreshToken,
		String tokenType,
		Instant expiresAt,
		UserResponse user
) {
	public static AuthResponse bearer(String accessToken, String refreshToken, Instant expiresAt, UserResponse user) {
		return new AuthResponse(accessToken, refreshToken, "Bearer", expiresAt, user);
	}
}
