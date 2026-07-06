package codehealthy.payflux.auditservice.errors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiErrorResponse> handleResponseStatus(
			ResponseStatusException exception,
			HttpServletRequest request
	) {
		int status = exception.getStatusCode().value();
		String message = message(exception.getReason(), HttpStatus.valueOf(status).getReasonPhrase());
		return ResponseEntity.status(status).body(ApiErrorResponse.of(status, codeFor(message), message, path(request)));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
			fieldErrors.put(fieldError.getField(), message(fieldError.getDefaultMessage(), "Invalid value"));
		}

		return ResponseEntity.badRequest().body(ApiErrorResponse.validation(
				HttpStatus.BAD_REQUEST.value(),
				"Request validation failed",
				path(request),
				fieldErrors
		));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleMalformedJson(HttpServletRequest request) {
		return ResponseEntity.badRequest().body(ApiErrorResponse.of(
				HttpStatus.BAD_REQUEST.value(),
				"MALFORMED_JSON",
				"Request body is missing or malformed",
				path(request)
		));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
			IllegalArgumentException exception,
			HttpServletRequest request
	) {
		String message = message(exception.getMessage(), "Invalid request");
		return ResponseEntity.badRequest().body(ApiErrorResponse.of(
				HttpStatus.BAD_REQUEST.value(),
				codeFor(message),
				message,
				path(request)
		));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleDataIntegrity(HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.of(
				HttpStatus.CONFLICT.value(),
				"DATA_INTEGRITY_VIOLATION",
				"Request conflicts with existing data",
				path(request)
		));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(HttpServletRequest request) {
		return ResponseEntity.internalServerError().body(ApiErrorResponse.of(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"INTERNAL_SERVER_ERROR",
				"Unexpected server error",
				path(request)
		));
	}

	private String codeFor(String message) {
		String normalized = message.toLowerCase();
		if (normalized.contains("email is already registered")) return "EMAIL_ALREADY_REGISTERED";
		if (normalized.contains("invalid email or password")) return "INVALID_CREDENTIALS";
		if (normalized.contains("too many failed login attempts")) return "TOO_MANY_LOGIN_ATTEMPTS";
		if (normalized.contains("token is missing userid")) return "TOKEN_MISSING_USER_ID";
		if (normalized.contains("account number not found")) return "ACCOUNT_NUMBER_NOT_FOUND";
		if (normalized.contains("beneficiary already exists")) return "BENEFICIARY_ALREADY_EXISTS";
		if (normalized.contains("own account")) return "OWN_ACCOUNT_NOT_ALLOWED";
		if (normalized.contains("beneficiary not found")) return "BENEFICIARY_NOT_FOUND";
		if (normalized.contains("wallet is not ready")) return "WALLET_NOT_READY";
		if (normalized.contains("cannot transfer to your own account")) return "SELF_TRANSFER_NOT_ALLOWED";
		if (normalized.contains("receiver account is not a payflux wallet")) return "RECEIVER_WALLET_NOT_FOUND";
		if (normalized.contains("insufficient wallet balance")) return "INSUFFICIENT_BALANCE";
		if (normalized.contains("transfer confirmation does not match")) return "TRANSFER_CONFIRMATION_MISMATCH";
		if (normalized.contains("wallet is not active")) return "WALLET_NOT_ACTIVE";
		if (normalized.contains("amount must be greater than zero")) return "INVALID_AMOUNT";
		if (normalized.contains("duplicate wallet operation")) return "DUPLICATE_WALLET_OPERATION";
		if (normalized.contains("transfer confirmation expired")) return "TRANSFER_CONFIRMATION_EXPIRED";
		if (normalized.contains("invalid transfer confirmation code")) return "INVALID_TRANSFER_CONFIRMATION_CODE";
		if (normalized.contains("transfer confirmation locked")) return "TRANSFER_CONFIRMATION_LOCKED";
		if (normalized.contains("transfer is already being processed")) return "TRANSFER_ALREADY_PROCESSING";
		if (normalized.contains("required")) return "REQUIRED_FIELD";
		if (normalized.contains("authorization failed")) return "DOWNSTREAM_AUTHORIZATION_FAILED";
		if (normalized.contains("service is unavailable")) return "DOWNSTREAM_SERVICE_UNAVAILABLE";
		if (normalized.contains("empty response")) return "DOWNSTREAM_EMPTY_RESPONSE";
		return "REQUEST_FAILED";
	}

	private String message(String value, String fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}

		return value;
	}

	private String path(HttpServletRequest request) {
		return request.getRequestURI();
	}
}
