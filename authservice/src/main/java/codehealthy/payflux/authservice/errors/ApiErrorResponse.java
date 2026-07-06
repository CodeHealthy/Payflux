package codehealthy.payflux.authservice.errors;

import java.time.Instant;
import java.util.Map;

import org.slf4j.MDC;

public record ApiErrorResponse(
		Instant timestamp,
		int status,
		String code,
		String message,
		String correlationId,
		String path,
		Map<String, String> fieldErrors
) {
	public static ApiErrorResponse of(int status, String code, String message, String path) {
		return new ApiErrorResponse(Instant.now(), status, code, message, currentCorrelationId(), path, null);
	}

	public static ApiErrorResponse validation(int status, String message, String path, Map<String, String> fieldErrors) {
		return new ApiErrorResponse(Instant.now(), status, "VALIDATION_ERROR", message, currentCorrelationId(), path, fieldErrors);
	}

	private static String currentCorrelationId() {
		return MDC.get("correlationId");
	}
}
