package cl.duoc.andesstay.audit.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import cl.duoc.andesstay.audit.domain.AuditEvent;
import cl.duoc.andesstay.audit.repo.AuditEventRepository;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditEventRepository repository;

    public AuditController(AuditEventRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/timeline")
    @PreAuthorize("hasAnyRole('Admin','Auditor')")
    public List<AuditEvent> timeline() {
        return repository.findAllByOrderByOccurredAtDesc();
    }
}
