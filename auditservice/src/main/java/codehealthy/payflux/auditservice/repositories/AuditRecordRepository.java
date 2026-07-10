package codehealthy.payflux.auditservice.repositories;

import codehealthy.payflux.auditservice.models.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.List;

public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long>, JpaSpecificationExecutor<AuditRecord> {
	boolean existsBySourceServiceAndSourceEventId(String sourceService, String sourceEventId);

	List<AuditRecord> findTop100ByOrderByCreatedAtDesc();

	long countByAction(String action);

	Optional<AuditRecord> findFirstByOrderByCreatedAtDesc();
}
