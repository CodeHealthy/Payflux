package codehealthy.payflux.walletservice.services;

import codehealthy.payflux.audit.events.AuditTrailEvent;
import codehealthy.payflux.events.AdminWalletStatusChangedEvent;
import codehealthy.payflux.events.TransferCompletedEvent;
import codehealthy.payflux.events.TransferDisputeStatusChangedEvent;
import codehealthy.payflux.events.TransferOtpRequestedEvent;
import codehealthy.payflux.walletservice.models.OutboxEvent;
import codehealthy.payflux.walletservice.repositories.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OutboxService {

	private final OutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;
	private final String transferCompletedTopic;
	private final String adminWalletStatusChangedTopic;
	private final String transferOtpRequestedTopic;
	private final String auditEventsTopic;
	private final String transferDisputeStatusChangedTopic;

	public OutboxService(
			OutboxEventRepository outboxEventRepository,
			ObjectMapper objectMapper,
			@Value("${app.kafka.topics.transfer-completed}") String transferCompletedTopic,
			@Value("${app.kafka.topics.admin-wallet-status-changed}") String adminWalletStatusChangedTopic,
			@Value("${app.kafka.topics.transfer-otp-requested}") String transferOtpRequestedTopic,
			@Value("${app.kafka.topics.audit-events}") String auditEventsTopic,
			@Value("${app.kafka.topics.transfer-dispute-status-changed}") String transferDisputeStatusChangedTopic) {
		this.outboxEventRepository = outboxEventRepository;
		this.objectMapper = objectMapper;
		this.transferCompletedTopic = transferCompletedTopic;
		this.adminWalletStatusChangedTopic = adminWalletStatusChangedTopic;
		this.transferOtpRequestedTopic = transferOtpRequestedTopic;
		this.auditEventsTopic = auditEventsTopic;
		this.transferDisputeStatusChangedTopic = transferDisputeStatusChangedTopic;
	}

	public void enqueueTransferCompleted(TransferCompletedEvent event) {
		outboxEventRepository.save(new OutboxEvent(
				event.eventId(),
				transferCompletedTopic,
				TransferCompletedEvent.class.getName(),
				"WALLET_TRANSFER",
				event.transactionReference(),
				toJson(event)));
	}

	public void enqueueAdminWalletStatusChanged(AdminWalletStatusChangedEvent event) {
		outboxEventRepository.save(new OutboxEvent(
				event.eventId(),
				adminWalletStatusChangedTopic,
				AdminWalletStatusChangedEvent.class.getName(),
				"WALLET",
				event.ownerUserId().toString(),
				toJson(event)));
	}

	public void enqueueTransferOtpRequested(TransferOtpRequestedEvent event) {
		outboxEventRepository.save(new OutboxEvent(
				event.eventId(),
				transferOtpRequestedTopic,
				TransferOtpRequestedEvent.class.getName(),
				"TRANSFER_OTP",
				event.ownerUserId().toString(),
				toJson(event)));
	}

	public void enqueueAuditTrail(AuditTrailEvent event) {
		outboxEventRepository.save(new OutboxEvent(
				event.eventId(),
				auditEventsTopic,
				AuditTrailEvent.class.getName(),
				event.aggregateType(),
				event.aggregateId(),
				toJson(event)));
	}

	public void enqueueTransferDisputeStatusChanged(TransferDisputeStatusChangedEvent event) {
		outboxEventRepository.save(new OutboxEvent(
				event.eventId(),
				transferDisputeStatusChangedTopic,
				TransferDisputeStatusChangedEvent.class.getName(),
				"TRANSFER_DISPUTE",
				event.disputeId().toString(),
				toJson(event)));
	}

	private String toJson(Object event) {
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Could not serialize outbox event", exception);
		}
	}
}
