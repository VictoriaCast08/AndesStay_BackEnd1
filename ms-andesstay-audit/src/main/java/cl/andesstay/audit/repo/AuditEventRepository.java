package cl.duoc.andesstay.audit.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import cl.duoc.andesstay.audit.domain.AuditEvent;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findAllByOrderByOccurredAtDesc();
}
