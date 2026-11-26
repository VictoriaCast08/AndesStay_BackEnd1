package cl.andesstay.report.kafka.dto;

import java.time.Instant;
import java.util.Map;

/**
 * DTO de consumo local para eventos de analítica de reservas.
 * Se deserializa desde andesstay.analytics.reservations.
 */
public record ReservationAnalyticsEventDto(
        String eventId,
        Instant timestamp,
        String action,
        String entityId,
        String userId,
        Map<String, Object> metadata) {
}
