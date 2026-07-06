package codehealthy.payflux.gatewayservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@Configuration
public class GatewayRoutesConfig {

	@Bean
	public RouterFunction<ServerResponse> authRoute(@Value("${payflux.services.auth}") String authServiceUrl) {
		return route("auth-service")
				.route(path("/auth/**"), http())
				.before(uri(authServiceUrl))
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> accountRoute(@Value("${payflux.services.account}") String accountServiceUrl) {
		return route("account-service")
				.route(path("/accounts/**"), http())
				.before(uri(accountServiceUrl))
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> notificationRoute(
			@Value("${payflux.services.notification}") String notificationServiceUrl
	) {
		return route("notification-service")
				.route(path("/notifications/**"), http())
				.before(uri(notificationServiceUrl))
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> walletRoute(@Value("${payflux.services.wallet}") String walletServiceUrl) {
		return route("wallet-service")
				.route(path("/wallets/**"), http())
				.before(uri(walletServiceUrl))
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> beneficiaryRoute(
			@Value("${payflux.services.beneficiary}") String beneficiaryServiceUrl
	) {
		return route("beneficiary-service")
				.route(path("/beneficiaries/**"), http())
				.before(uri(beneficiaryServiceUrl))
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> transactionRoute(
			@Value("${payflux.services.transaction}") String transactionServiceUrl
	) {
		return route("transaction-service")
				.route(path("/transactions/**"), http())
				.before(uri(transactionServiceUrl))
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> auditRoute(@Value("${payflux.services.audit}") String auditServiceUrl) {
		return route("audit-service")
				.route(path("/audit-records/**"), http())
				.before(uri(auditServiceUrl))
				.build();
	}
}
