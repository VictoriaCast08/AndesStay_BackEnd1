package cl.andesstay.audit.kafka;

import cl.andesstay.audit.kafka.dto.AuditEventDto;
import cl.andesstay.audit.service.AuditEventService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumidor Kafka de auditoría (infraestructura de mensajería).
 * Escucha andesstay.audit.events con groupId andesstay-audit-group.
 * La lógica de negocio reside en AuditEventService.
 */
@Component
public class AuditEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditEventConsumer.class);

    private static final String GROUP_ID = "andesstay-audit-group";

    private final AuditEventService auditEventService;

    public AuditEventConsumer(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_AUDIT_EVENTS,
            groupId = GROUP_ID)
    public void onAuditEvent(@Payload AuditEventDto evento,
                             ConsumerRecord<String, Object> record,
                             Acknowledgment ack) {
        String topic = record.topic();
        int partition = record.partition();
        long offset = record.offset();
        String eventId = evento != null ? evento.eventId() : "DESCONOCIDO";

        log.info("[Consumer][audit] Mensaje recibido topic={} partition={} offset={} eventId={}",
                topic, partition, offset, eventId);

        // Validación de payload obligatorio
        if (evento == null || evento.eventId() == null || evento.action() == null || evento.entityId() == null) {
            String motivo = "Payload inválido: faltan campos obligatorios (eventId, action, entityId)";
            log.error("[Consumer][audit] {}. topic={} partition={} offset={} eventId={}",
                    motivo, topic, partition, offset, eventId);
            throw new IllegalArgumentException(motivo);
        }

        // Delegar a la lógica de dominio (separada de la infraestructura)
        auditEventService.registrarEvento(evento);

        // Confirmar offset manualmente (enable-auto-commit=false)
        ack.acknowledge();
        log.debug("[Consumer][audit] Offset confirmado topic={} partition={} offset={} eventId={}",
                topic, partition, offset, eventId);
    }
}
