package codehealthy.payflux.walletservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

	@Bean
	public NewTopic transferCompletedTopic(@Value("${app.kafka.topics.transfer-completed}") String topicName) {
		return TopicBuilder.name(topicName)
				.partitions(1)
				.replicas(1)
				.build();
	}

	@Bean
	public NewTopic adminWalletStatusChangedTopic(
			@Value("${app.kafka.topics.admin-wallet-status-changed}") String topicName
	) {
		return TopicBuilder.name(topicName)
				.partitions(1)
				.replicas(1)
				.build();
	}
}
