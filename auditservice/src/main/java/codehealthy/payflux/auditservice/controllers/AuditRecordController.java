package codehealthy.payflux.auditservice.controllers;

import codehealthy.payflux.auditservice.dto.AuditRecordResponse;
import codehealthy.payflux.auditservice.dto.AuditSummaryResponse;
import codehealthy.payflux.auditservice.services.AuditService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/audit-records")
public class AuditRecordController {

	private final AuditService auditService;

	public AuditRecordController(AuditService auditService) {
		this.auditService = auditService;
	}

	@GetMapping
	public List<AuditRecordResponse> findRecentRecords(
			@RequestParam(required = false) String action,
			@RequestParam(required = false) Long actorUserId,
			@RequestParam(required = false) Long subjectUserId,
			@RequestParam(required = false) String sourceService,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
	) {
		return auditService.findRecords(action, actorUserId, subjectUserId, sourceService, keyword, from, to);
	}

	@GetMapping("/summary")
	public AuditSummaryResponse getSummary() {
		return auditService.getSummary();
	}
}
