package cl.andesstay.report.kafka;

import cl.andesstay.report.kafka.dto.ReservationAnalyticsEventDto;
import cl.andesstay.report.service.ReservationAnalyticsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumidor Kafka de analítica de reportes (infraestructura de mensajería).
 * Escucha andesstay.analytics.reservations con groupId andesstay-analytics-group.
 * La lógica de negocio reside en ReservationAnalyticsService.
 */
@Component
public class ReservationAnalyticsConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReservationAnalyticsConsumer.class);

    private static final String GROUP_ID = "andesstay-analytics-group";

    private final ReservationAnalyticsService analyticsService;

    public ReservationAnalyticsConsumer(ReservationAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_ANALYTICS_RESERVATIONS,
            groupId = GROUP_ID)
    public void onReservationEvent(@Payload ReservationAnalyticsEventDto evento,
                                   ConsumerRecord<String, Object> record,
                                   Acknowledgment ack) {
        String topic = record.topic();
        int partition = record.partition();
        long offset = record.offset();
        String eventId = evento != null ? evento.eventId() : "DESCONOCIDO";

        log.info("[Consumer][report] Mensaje recibido topic={} partition={} offset={} eventId={}",
                topic, partition, offset, eventId);

        // Validación de payload obligatorio
        if (evento == null || evento.eventId() == null || evento.action() == null || evento.entityId() == null) {
            String motivo = "Payload inválido: faltan campos obligatorios (eventId, action, entityId)";
            log.error("[Consumer][report] {}. topic={} partition={} offset={} eventId={}",
                    motivo, topic, partition, offset, eventId);
            throw new IllegalArgumentException(motivo);
        }

        // Delegar a la lógica de dominio (separada de la infraestructura)
        analyticsService.procesarEvento(evento);

        // Confirmar offset manualmente (enable-auto-commit=false)
        ack.acknowledge();
        log.debug("[Consumer][report] Offset confirmado topic={} partition={} offset={} eventId={}",
                topic, partition, offset, eventId);
    }
}
