package codehealthy.payflux.beneficiaryservice.services;

import codehealthy.payflux.beneficiaryservice.events.BeneficiaryAddedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BeneficiaryEventPublisher {

	private final KafkaTemplate<String, BeneficiaryAddedEvent> kafkaTemplate;
	private final String beneficiaryAddedTopic;

	public BeneficiaryEventPublisher(
			KafkaTemplate<String, BeneficiaryAddedEvent> kafkaTemplate,
			@Value("${app.kafka.topics.beneficiary-added}") String beneficiaryAddedTopic
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.beneficiaryAddedTopic = beneficiaryAddedTopic;
	}

	public void publishBeneficiaryAdded(BeneficiaryAddedEvent event) {
		kafkaTemplate.send(beneficiaryAddedTopic, event.ownerUserId().toString(), event);
	}
}
