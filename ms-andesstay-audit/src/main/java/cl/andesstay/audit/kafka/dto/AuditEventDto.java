package cl.andesstay.audit.kafka.dto;

import java.time.Instant;
import java.util.Map;

/**
 * DTO de consumo local para eventos de auditoría.
 * Se deserializa desde andesstay.audit.events.
 */
public record AuditEventDto(
        String eventId,
        Instant timestamp,
        String action,
        String entityId,
        String userId,
        Map<String, Object> metadata) {
}
