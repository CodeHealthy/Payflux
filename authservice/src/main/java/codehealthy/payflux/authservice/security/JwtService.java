package codehealthy.payflux.authservice.security;

import codehealthy.payflux.authservice.models.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

	private final JwtEncoder jwtEncoder;
	private final String issuer;
	private final long accessTokenExpirationMinutes;

	public JwtService(
			JwtEncoder jwtEncoder,
			@Value("${app.jwt.issuer}") String issuer,
			@Value("${app.jwt.access-token-expiration-minutes}") long accessTokenExpirationMinutes
	) {
		this.jwtEncoder = jwtEncoder;
		this.issuer = issuer;
		this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
	}

	public GeneratedToken generateAccessToken(AppUser user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES);

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(issuer)
				.issuedAt(now)
				.expiresAt(expiresAt)
				.subject(user.getEmail())
				.claim("userId", user.getId())
				.claim("fullName", user.getFullName())
				.claim("role", user.getRole().name())
				.build();

		JwsHeader header = JwsHeader.with(() -> "HS256").build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new GeneratedToken(token, expiresAt);
	}

	public record GeneratedToken(String value, Instant expiresAt) {
	}
}
