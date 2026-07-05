package codehealthy.payflux.accountservice.services;

import codehealthy.payflux.accountservice.models.Account;
import codehealthy.payflux.events.AccountCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AccountEventPublisher {

	private final KafkaTemplate<String, AccountCreatedEvent> kafkaTemplate;
	private final String accountCreatedTopic;

	public AccountEventPublisher(
			KafkaTemplate<String, AccountCreatedEvent> kafkaTemplate,
			@Value("${app.kafka.topics.account-created}") String accountCreatedTopic
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.accountCreatedTopic = accountCreatedTopic;
	}

	public void publishAccountCreated(Account account) {
		AccountCreatedEvent event = new AccountCreatedEvent(
				account.getId(),
				account.getOwnerUserId(),
				account.getAccountNumber(),
				account.getFullName(),
				account.getEmail(),
				account.getCreatedAt()
		);

		kafkaTemplate.send(accountCreatedTopic, account.getId().toString(), event);
	}
}
