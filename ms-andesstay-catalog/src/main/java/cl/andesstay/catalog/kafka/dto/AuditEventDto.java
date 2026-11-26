package cl.andesstay.catalog.kafka.dto;

import java.time.Instant;
import java.util.Map;

/**
 * DTO inmutable de evento de auditoría del catálogo.
 * Se publica en el tópico Kafka andesstay.audit.events.
 */
public record AuditEventDto(
        String eventId,
        Instant timestamp,
        String action,
        String entityId,
        String userId,
        Map<String, Object> metadata) {
}
