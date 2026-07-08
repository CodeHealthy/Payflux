package codehealthy.payflux.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

	@Bean
	public JavaMailSender javaMailSender(
			@Value("${spring.mail.host:localhost}") String host,
			@Value("${spring.mail.port:1025}") int port,
			@Value("${spring.mail.username:}") String username,
			@Value("${spring.mail.password:}") String password,
			@Value("${spring.mail.properties.mail.smtp.auth:true}") String smtpAuth,
			@Value("${spring.mail.properties.mail.smtp.starttls.enable:true}") String startTlsEnabled,
			@Value("${spring.mail.properties.mail.smtp.connectiontimeout:5000}") String connectionTimeout,
			@Value("${spring.mail.properties.mail.smtp.timeout:5000}") String timeout,
			@Value("${spring.mail.properties.mail.smtp.writetimeout:5000}") String writeTimeout
	) {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(host);
		mailSender.setPort(port);
		mailSender.setUsername(username);
		mailSender.setPassword(password);

		Properties properties = mailSender.getJavaMailProperties();
		properties.put("mail.smtp.auth", smtpAuth);
		properties.put("mail.smtp.starttls.enable", startTlsEnabled);
		properties.put("mail.smtp.connectiontimeout", connectionTimeout);
		properties.put("mail.smtp.timeout", timeout);
		properties.put("mail.smtp.writetimeout", writeTimeout);
		return mailSender;
	}
}
