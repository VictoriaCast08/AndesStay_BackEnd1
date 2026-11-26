package cl.andesstay.reservations.kafka.dto;

import java.time.Instant;
import java.util.Map;

/**
 * DTO inmutable de evento de analítica de reservas.
 * Se publica en el tópico Kafka andesstay.analytics.reservations.
 */
public record ReservationAnalyticsEventDto(
        String eventId,
        Instant timestamp,
        String action,
        String entityId,
        String userId,
        Map<String, Object> metadata) {
}
