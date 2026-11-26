package cl.andesstay.audit.service;

import cl.andesstay.audit.domain.AuditEvent;
import cl.andesstay.audit.kafka.dto.AuditEventDto;
import cl.andesstay.audit.repo.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de dominio de auditoría (separada de la infraestructura Kafka).
 * Registra trazabilidad histórica de eventos en PostgreSQL.
 */
@Service
public class AuditEventService {

    private static final Logger log = LoggerFactory.getLogger(AuditEventService.class);

    private final AuditEventRepository repository;

    public AuditEventService(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void registrarEvento(AuditEventDto evento) {
        if (evento == null || evento.eventId() == null || evento.action() == null || evento.entityId() == null) {
            throw new IllegalArgumentException("Evento de auditoría inválido: faltan campos obligatorios");
        }

        AuditEvent entity = new AuditEvent();
        entity.setEventId(evento.eventId());
        entity.setReservationCode(evento.entityId());
        entity.setEventType(evento.action());
        entity.setActor(evento.userId());
        entity.setOrigin("kafka");
        entity.setOccurredAt(evento.timestamp() != null ? evento.timestamp() : java.time.Instant.now());

        repository.save(entity);
        log.info("[Audit] Evento persistido eventId={} action={} entityId={} userId={}",
                evento.eventId(), evento.action(), evento.entityId(), evento.userId());
    }
}
