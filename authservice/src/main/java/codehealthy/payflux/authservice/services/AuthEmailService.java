package codehealthy.payflux.authservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AuthEmailService {

	private static final Logger log = LoggerFactory.getLogger(AuthEmailService.class);

	private final JavaMailSender mailSender;
	private final String provider;
	private final String fromAddress;
	private final boolean enabled;

	public AuthEmailService(
			JavaMailSender mailSender,
			@Value("${app.email.provider:none}") String provider,
			@Value("${app.email.from:no-reply@payflux.local}") String fromAddress,
			@Value("${app.email.enabled:true}") boolean enabled
	) {
		this.mailSender = mailSender;
		this.provider = provider;
		this.fromAddress = fromAddress;
		this.enabled = enabled;
	}

	public void sendVerificationCode(String recipient, String fullName, String code, long ttlMinutes) {
		send(
				recipient,
				"Verify your PayFlux email",
				"""
						Hello %s,

						Use this code to verify your PayFlux email:

						%s

						This code expires in %d minutes.

						If you did not create a PayFlux profile, you can ignore this email.
						""".formatted(fullName, code, ttlMinutes)
		);
	}

	public void sendPasswordResetCode(String recipient, String fullName, String code, long ttlMinutes) {
		send(
				recipient,
				"Reset your PayFlux password",
				"""
						Hello %s,

						Use this code to reset your PayFlux password:

						%s

						This code expires in %d minutes.

						If you did not request a password reset, keep your current password and ignore this email.
						""".formatted(fullName, code, ttlMinutes)
		);
	}

	private void send(String recipient, String subject, String body) {
		if (!enabled || !"smtp".equalsIgnoreCase(provider)) {
			log.info("Auth email delivery skipped for recipient {} because provider is {}", recipient, provider);
			return;
		}

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromAddress);
			message.setTo(recipient);
			message.setSubject(subject);
			message.setText(body);
			mailSender.send(message);
		} catch (MailException exception) {
			log.error("Auth email delivery failed for recipient {}", recipient, exception);
		}
	}
}
