package codehealthy.payflux.authservice.controllers;

import codehealthy.payflux.authservice.dto.AuthResponse;
import codehealthy.payflux.authservice.dto.ForgotPasswordRequest;
import codehealthy.payflux.authservice.dto.LoginRequest;
import codehealthy.payflux.authservice.dto.PasswordRecoveryQuestionResponse;
import codehealthy.payflux.authservice.dto.RefreshTokenRequest;
import codehealthy.payflux.authservice.dto.RegistrationResponse;
import codehealthy.payflux.authservice.dto.RegisterRequest;
import codehealthy.payflux.authservice.dto.ResetPasswordRequest;
import codehealthy.payflux.authservice.dto.UpdatePasswordRequest;
import codehealthy.payflux.authservice.dto.UpdateProfileRequest;
import codehealthy.payflux.authservice.dto.UpdateSecurityQuestionRequest;
import codehealthy.payflux.authservice.dto.UserResponse;
import codehealthy.payflux.authservice.dto.VerifyEmailRequest;
import codehealthy.payflux.authservice.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public RegistrationResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping("/verify-email")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
		authService.verifyEmail(request);
	}

	@PostMapping("/resend-verification")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void resendEmailVerification(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.resendEmailVerification(request);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
		return authService.login(request, clientIp(servletRequest));
	}

	@PostMapping("/refresh")
	public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return authService.refresh(request);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(@Valid @RequestBody RefreshTokenRequest request) {
		authService.logout(request);
	}

	@PostMapping("/forgot-password")
	public PasswordRecoveryQuestionResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		return authService.recoveryQuestion(request);
	}

	@PostMapping("/reset-password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		authService.resetPassword(request);
	}

	@GetMapping("/me")
	public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
		return authService.me(currentUserId(jwt));
	}

	@PatchMapping("/me/profile")
	public UserResponse updateProfile(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody UpdateProfileRequest request
	) {
		return authService.updateProfile(currentUserId(jwt), request);
	}

	@PatchMapping("/me/password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updatePassword(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody UpdatePasswordRequest request
	) {
		authService.updatePassword(currentUserId(jwt), request);
	}

	@PatchMapping("/me/security-question")
	public UserResponse updateSecurityQuestion(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody UpdateSecurityQuestionRequest request
	) {
		return authService.updateSecurityQuestion(currentUserId(jwt), request);
	}

	private String clientIp(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}

		return request.getRemoteAddr();
	}

	private Long currentUserId(Jwt jwt) {
		return jwt.getClaim("userId");
	}
}
