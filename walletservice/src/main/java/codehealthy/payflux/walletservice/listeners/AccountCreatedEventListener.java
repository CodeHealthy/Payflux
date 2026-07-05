package codehealthy.payflux.walletservice.listeners;

import codehealthy.payflux.events.AccountCreatedEvent;
import codehealthy.payflux.walletservice.services.WalletService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AccountCreatedEventListener {

	private final WalletService walletService;

	public AccountCreatedEventListener(WalletService walletService) {
		this.walletService = walletService;
	}

	@KafkaListener(topics = "${app.kafka.topics.account-created}", groupId = "${spring.kafka.consumer.group-id}")
	public void handleAccountCreated(AccountCreatedEvent event) {
		walletService.provisionWallet(event);
	}
}
