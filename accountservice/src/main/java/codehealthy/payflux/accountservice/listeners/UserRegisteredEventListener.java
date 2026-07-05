package codehealthy.payflux.accountservice.listeners;

import codehealthy.payflux.accountservice.services.AccountService;
import codehealthy.payflux.authservice.events.UserRegisteredEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredEventListener {

	private final AccountService accountService;

	public UserRegisteredEventListener(AccountService accountService) {
		this.accountService = accountService;
	}

	@KafkaListener(topics = "${app.kafka.topics.user-registered}", groupId = "${spring.kafka.consumer.group-id}")
	public void handleUserRegistered(UserRegisteredEvent event) {
		accountService.createAccountForRegisteredUser(event.userId(), event.fullName(), event.email());
	}
}
