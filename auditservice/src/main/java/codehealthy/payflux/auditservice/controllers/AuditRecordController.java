package codehealthy.payflux.auditservice.controllers;

import codehealthy.payflux.auditservice.dto.AuditRecordResponse;
import codehealthy.payflux.auditservice.services.AuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit-records")
public class AuditRecordController {

	private final AuditService auditService;

	public AuditRecordController(AuditService auditService) {
		this.auditService = auditService;
	}

	@GetMapping
	public List<AuditRecordResponse> findRecentRecords() {
		return auditService.findRecentRecords();
	}
}
