package codehealthy.payflux.auditservice.repositories;

import codehealthy.payflux.auditservice.models.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {
	boolean existsBySourceServiceAndSourceEventId(String sourceService, String sourceEventId);

	List<AuditRecord> findTop100ByOrderByCreatedAtDesc();
}
