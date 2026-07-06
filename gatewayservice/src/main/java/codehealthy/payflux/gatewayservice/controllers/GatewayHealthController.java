package codehealthy.payflux.gatewayservice.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class GatewayHealthController {

	@GetMapping("/gateway/health")
	public Map<String, Object> health() {
		return Map.of(
				"service", "gatewayservice",
				"status", "UP",
				"timestamp", Instant.now()
		);
	}
}
