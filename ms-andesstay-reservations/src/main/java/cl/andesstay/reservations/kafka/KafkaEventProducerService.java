package cl.andesstay.reservations.kafka;

import cl.andesstay.reservations.domain.Reservation;
import cl.andesstay.reservations.kafka.dto.AuditEventDto;
import cl.andesstay.reservations.kafka.dto.ReservationAnalyticsEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Productor Kafka dedicado para el microservicio de reservas.
 * Publica eventos en andesstay.analytics.reservations y andesstay.audit.events
 * de forma asíncrona, sin bloquear la transacción de negocio ni alterar RabbitMQ.
 */
@Service
public class KafkaEventProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventProducerService.class);

    private static final String TOPIC_ANALYTICS = "andesstay.analytics.reservations";
    private static final String TOPIC_AUDIT = "andesstay.audit.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishReservationCreated(Reservation reservation, String userId) {
        ReservationAnalyticsEventDto analytics = event("RESERVATION_CREATED", reservation.getCode(), userId,
                analyticsMetadata(reservation));
        AuditEventDto audit = eventAudit("RESERVATION_CREATED", reservation.getCode(), userId,
                auditMetadata(reservation));
        send(TOPIC_ANALYTICS, reservation.getCode(), analytics);
        send(TOPIC_AUDIT, reservation.getCode(), audit);
    }

    public void publishReservationConfirmed(Reservation reservation, String userId) {
        ReservationAnalyticsEventDto analytics = event("RESERVATION_CONFIRMED", reservation.getCode(), userId,
                analyticsMetadata(reservation));
        AuditEventDto audit = eventAudit("RESERVATION_CONFIRMED", reservation.getCode(), userId,
                auditMetadata(reservation));
        send(TOPIC_ANALYTICS, reservation.getCode(), analytics);
        send(TOPIC_AUDIT, reservation.getCode(), audit);
    }

    public void publishReservationCancelled(Reservation reservation, String userId) {
        ReservationAnalyticsEventDto analytics = event("RESERVATION_CANCELLED", reservation.getCode(), userId,
                analyticsMetadata(reservation));
        AuditEventDto audit = eventAudit("RESERVATION_CANCELLED", reservation.getCode(), userId,
                auditMetadata(reservation));
        send(TOPIC_ANALYTICS, reservation.getCode(), analytics);
        send(TOPIC_AUDIT, reservation.getCode(), audit);
    }

    private ReservationAnalyticsEventDto event(String action, String entityId, String userId,
                                               Map<String, Object> metadata) {
        return new ReservationAnalyticsEventDto(
                UUID.randomUUID().toString(),
                Instant.now(),
                action,
                entityId,
                userId,
                metadata);
    }

    private AuditEventDto eventAudit(String action, String entityId, String userId,
                                     Map<String, Object> metadata) {
        return new AuditEventDto(
                UUID.randomUUID().toString(),
                Instant.now(),
                action,
                entityId,
                userId,
                metadata);
    }

    private Map<String, Object> analyticsMetadata(Reservation reservation) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", reservation.getCode());
        m.put("unitId", reservation.getUnitId());
        m.put("guestName", reservation.getGuestName());
        m.put("status", reservation.getStatus() != null ? reservation.getStatus().name() : null);
        m.put("stayFrom", reservation.getStayFrom() != null ? reservation.getStayFrom().toString() : null);
        m.put("stayTo", reservation.getStayTo() != null ? reservation.getStayTo().toString() : null);
        return m;
    }

    private Map<String, Object> auditMetadata(Reservation reservation) {
        Map<String, Object> m = new HashMap<>();
        m.put("entityType", "Reservation");
        m.put("code", reservation.getCode());
        m.put("unitId", reservation.getUnitId());
        m.put("status", reservation.getStatus() != null ? reservation.getStatus().name() : null);
        return m;
    }

    private void send(String topic, String key, Object payload) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, payload);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("[Kafka] Fallo al enviar evento topic={} key={} payload={} error={}",
                        topic, key, payload, ex.getMessage());
            } else {
                log.info("[Kafka] Evento enviado OK topic={} partition={} offset={} key={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        result.getProducerRecord().key());
            }
        });
    }
}
