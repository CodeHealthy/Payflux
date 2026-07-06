package codehealthy.payflux.gatewayservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(requests -> requests
						.requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/refresh", "/auth/logout").permitAll()
						.requestMatchers("/gateway/health", "/actuator/health", "/actuator/info").permitAll()
						.requestMatchers("/audit-records/**").hasRole("ADMIN")
						.requestMatchers("/wallets/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated()
				)
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
				.build();
	}

	@Bean
	public JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String secret) {
		return NimbusJwtDecoder.withSecretKey(secretKey(secret))
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
	}

	private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
		return jwt -> {
			String role = jwt.getClaimAsString("role");
			List<SimpleGrantedAuthority> authorities = role == null
					? List.of()
					: List.of(new SimpleGrantedAuthority("ROLE_" + role));
			return new JwtAuthenticationToken(jwt, authorities);
		};
	}

	private SecretKeySpec secretKey(String secret) {
		return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}
}
