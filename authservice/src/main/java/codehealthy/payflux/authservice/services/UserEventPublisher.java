package codehealthy.payflux.authservice.services;

import codehealthy.payflux.authservice.events.UserRegisteredEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventPublisher {

	private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;
	private final String userRegisteredTopic;

	public UserEventPublisher(
			KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate,
			@Value("${app.kafka.topics.user-registered}") String userRegisteredTopic
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.userRegisteredTopic = userRegisteredTopic;
	}

	public void publishUserRegistered(UserRegisteredEvent event) {
		kafkaTemplate.send(userRegisteredTopic, event.email(), event);
	}
}
