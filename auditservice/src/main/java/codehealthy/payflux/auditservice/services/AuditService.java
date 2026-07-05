package codehealthy.payflux.auditservice.services;

import codehealthy.payflux.auditservice.dto.AuditRecordResponse;
import codehealthy.payflux.auditservice.models.AuditRecord;
import codehealthy.payflux.auditservice.repositories.AuditRecordRepository;
import codehealthy.payflux.authservice.events.UserRegisteredEvent;
import codehealthy.payflux.beneficiaryservice.events.BeneficiaryAddedEvent;
import codehealthy.payflux.events.TransferCompletedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AuditService {

	private final AuditRecordRepository auditRecordRepository;
	private final ObjectMapper objectMapper;

	public AuditService(AuditRecordRepository auditRecordRepository, ObjectMapper objectMapper) {
		this.auditRecordRepository = auditRecordRepository;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public void recordUserRegistered(UserRegisteredEvent event) {
		saveIfNew(new AuditRecord(
				"authservice",
				"USER_REGISTERED-" + event.userId(),
				"USER_REGISTERED",
				event.userId(),
				event.userId(),
				"USER",
				event.userId().toString(),
				"User registered with email " + event.email(),
				toJson(event),
				event.registeredAt()
		));
	}

	@Transactional
	public void recordBeneficiaryAdded(BeneficiaryAddedEvent event) {
		saveIfNew(new AuditRecord(
				"beneficiaryservice",
				"BENEFICIARY_ADDED-" + event.beneficiaryId(),
				"BENEFICIARY_ADDED",
				event.ownerUserId(),
				event.ownerUserId(),
				"BENEFICIARY",
				event.beneficiaryId().toString(),
				"Beneficiary " + event.nickname() + " added for account " + event.beneficiaryAccountNumber(),
				toJson(event),
				event.createdAt()
		));
	}

	@Transactional
	public void recordTransferCompleted(TransferCompletedEvent event) {
		saveIfNew(new AuditRecord(
				"walletservice",
				event.eventId(),
				"TRANSFER_COMPLETED",
				event.senderUserId(),
				event.receiverUserId(),
				"TRANSFER",
				event.transactionReference(),
				"Transfer " + event.transactionReference() + " completed for " + event.currency() + " " + event.amount(),
				toJson(event),
				event.completedAt()
		));
	}

	@Transactional(readOnly = true)
	public List<AuditRecordResponse> findRecentRecords() {
		return auditRecordRepository.findTop100ByOrderByCreatedAtDesc()
				.stream()
				.map(AuditRecordResponse::from)
				.toList();
	}

	private void saveIfNew(AuditRecord auditRecord) {
		if (auditRecordRepository.existsBySourceServiceAndSourceEventId(
				auditRecord.getSourceService(),
				auditRecord.getSourceEventId()
		)) {
			return;
		}

		auditRecordRepository.save(auditRecord);
	}

	private String toJson(Object event) {
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Could not serialize audit event", exception);
		}
	}
}
