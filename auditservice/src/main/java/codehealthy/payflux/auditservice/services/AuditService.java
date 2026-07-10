package codehealthy.payflux.auditservice.services;

import codehealthy.payflux.audit.events.AuditTrailEvent;
import codehealthy.payflux.auditservice.dto.AuditRecordResponse;
import codehealthy.payflux.auditservice.dto.AuditSummaryResponse;
import codehealthy.payflux.auditservice.models.AuditRecord;
import codehealthy.payflux.auditservice.repositories.AuditRecordRepository;
import codehealthy.payflux.authservice.events.UserRegisteredEvent;
import codehealthy.payflux.beneficiaryservice.events.BeneficiaryAddedEvent;
import codehealthy.payflux.events.AdminWalletStatusChangedEvent;
import codehealthy.payflux.events.TransferCompletedEvent;
import codehealthy.payflux.events.TransferOtpRequestedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
public class AuditService {

	private final AuditRecordRepository auditRecordRepository;
	private final ObjectMapper objectMapper;

	public AuditService(AuditRecordRepository auditRecordRepository, ObjectMapper objectMapper) {
		this.auditRecordRepository = auditRecordRepository;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public void recordAuditTrailEvent(AuditTrailEvent event) {
		saveIfNew(new AuditRecord(
				event.sourceService(),
				event.eventId(),
				event.action(),
				event.actorUserId(),
				event.subjectUserId(),
				event.aggregateType(),
				event.aggregateId(),
				event.summary(),
				toJson(event.details()),
				event.occurredAt()
		));
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
	public void recordTransferOtpRequested(TransferOtpRequestedEvent event) {
		saveIfNew(new AuditRecord(
				"walletservice",
				event.eventId(),
				"TRANSFER_OTP_REQUESTED",
				event.ownerUserId(),
				event.ownerUserId(),
				"TRANSFER_CONFIRMATION",
				event.eventId(),
				"Transfer OTP requested for " + event.currency() + " " + event.amount()
						+ " to " + event.receiverAccountNumber(),
				toJson(Map.of(
						"ownerUserId", event.ownerUserId(),
						"receiverName", event.receiverName(),
						"receiverAccountNumber", event.receiverAccountNumber(),
						"amount", event.amount(),
						"currency", event.currency(),
						"expiresAt", event.expiresAt()
				)),
				event.requestedAt()
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

	@Transactional
	public void recordAdminWalletStatusChanged(AdminWalletStatusChangedEvent event) {
		String action = "SUSPENDED".equals(event.newStatus()) ? "WALLET_SUSPENDED" : "WALLET_ACTIVATED";
		saveIfNew(new AuditRecord(
				"walletservice",
				event.eventId(),
				action,
				event.adminUserId(),
				event.ownerUserId(),
				"WALLET",
				event.accountNumber(),
				"Wallet " + event.accountNumber() + " changed from " + event.previousStatus()
						+ " to " + event.newStatus() + ". Reason: " + event.reason(),
				toJson(event),
				event.changedAt()
		));
	}

	@Transactional(readOnly = true)
	public List<AuditRecordResponse> findRecentRecords() {
		return auditRecordRepository.findTop100ByOrderByCreatedAtDesc()
				.stream()
				.map(AuditRecordResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<AuditRecordResponse> findRecords(
			String action,
			Long actorUserId,
			Long subjectUserId,
			String sourceService,
			String keyword,
			LocalDate from,
			LocalDate to
	) {
		Specification<AuditRecord> specification = Specification
				.where(equalsIgnoreCase("action", action))
				.and(equalsNumber("actorUserId", actorUserId))
				.and(equalsNumber("subjectUserId", subjectUserId))
				.and(equalsIgnoreCase("sourceService", sourceService))
				.and(createdAtFrom(from))
				.and(createdAtTo(to))
				.and(keyword(keyword));

		return auditRecordRepository.findAll(
						specification,
						PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"))
				)
				.stream()
				.map(AuditRecordResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public AuditSummaryResponse getSummary() {
		return new AuditSummaryResponse(
				auditRecordRepository.count(),
				auditRecordRepository.countByAction("USER_REGISTERED"),
				auditRecordRepository.countByAction("TRANSFER_COMPLETED"),
				auditRecordRepository.countByAction("BENEFICIARY_ADDED"),
				auditRecordRepository.findFirstByOrderByCreatedAtDesc()
						.map(AuditRecord::getCreatedAt)
						.orElse(null)
		);
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

	private Specification<AuditRecord> equalsIgnoreCase(String fieldName, String value) {
		return (root, query, criteriaBuilder) -> {
			if (value == null || value.isBlank()) {
				return criteriaBuilder.conjunction();
			}

			return criteriaBuilder.equal(
					criteriaBuilder.lower(root.get(fieldName)),
					value.trim().toLowerCase()
			);
		};
	}

	private Specification<AuditRecord> equalsNumber(String fieldName, Long value) {
		return (root, query, criteriaBuilder) -> value == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.equal(root.get(fieldName), value);
	}

	private Specification<AuditRecord> createdAtFrom(LocalDate from) {
		return (root, query, criteriaBuilder) -> from == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay().toInstant(ZoneOffset.UTC));
	}

	private Specification<AuditRecord> createdAtTo(LocalDate to) {
		return (root, query, criteriaBuilder) -> to == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
	}

	private Specification<AuditRecord> keyword(String keyword) {
		return (root, query, criteriaBuilder) -> {
			if (keyword == null || keyword.isBlank()) {
				return criteriaBuilder.conjunction();
			}

			String pattern = "%" + keyword.trim().toLowerCase() + "%";
			return criteriaBuilder.or(
					criteriaBuilder.like(criteriaBuilder.lower(root.get("summary")), pattern),
					criteriaBuilder.like(criteriaBuilder.lower(root.get("aggregateId")), pattern),
					criteriaBuilder.like(criteriaBuilder.lower(root.get("sourceEventId")), pattern)
			);
		};
	}
}
