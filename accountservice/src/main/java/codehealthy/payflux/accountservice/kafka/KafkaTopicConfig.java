package codehealthy.payflux.accountservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

	@Bean
	public NewTopic accountCreatedTopic(@Value("${app.kafka.topics.account-created}") String topicName) {
		return TopicBuilder.name(topicName)
				.partitions(1)
				.replicas(1)
				.build();
	}

	@Bean
	public NewTopic userRegisteredTopic(@Value("${app.kafka.topics.user-registered}") String topicName) {
		return TopicBuilder.name(topicName)
				.partitions(1)
				.replicas(1)
				.build();
	}
}
