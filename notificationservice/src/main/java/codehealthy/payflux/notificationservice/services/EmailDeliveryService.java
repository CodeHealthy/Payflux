package codehealthy.payflux.notificationservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailDeliveryService {

	private static final Logger log = LoggerFactory.getLogger(EmailDeliveryService.class);

	private final JavaMailSender mailSender;
	private final String provider;
	private final String fromAddress;
	private final boolean enabled;

	public EmailDeliveryService(
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

	public void send(String recipient, String subject, String body) {
		if (!enabled || !"smtp".equalsIgnoreCase(provider)) {
			log.info("Email delivery skipped for recipient {} because provider is {}", recipient, provider);
			return;
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromAddress);
		message.setTo(recipient);
		message.setSubject(subject);
		message.setText(body);

		try {
			mailSender.send(message);
		} catch (MailException exception) {
			log.error("Email delivery failed for recipient {}", recipient, exception);
		}
	}
}
