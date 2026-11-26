package cl.andesstay.catalog.kafka;

import cl.andesstay.catalog.domain.Unit;
import cl.andesstay.catalog.kafka.dto.AuditEventDto;
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
 * Productor Kafka dedicado para el microservicio de catálogo.
 * Publica eventos de auditoría en andesstay.audit.events de forma asíncrona.
 */
@Service
public class KafkaEventProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventProducerService.class);

    private static final String TOPIC_AUDIT = "andesstay.audit.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUnitCreated(Unit unit, String userId) {
        AuditEventDto audit = event("UNIT_CREATED", unit.getCode(), userId, auditMetadata(unit));
        send(TOPIC_AUDIT, unit.getCode(), audit);
    }

    public void publishUnitUpdated(Unit unit, String userId) {
        AuditEventDto audit = event("UNIT_UPDATED", unit.getCode(), userId, auditMetadata(unit));
        send(TOPIC_AUDIT, unit.getCode(), audit);
    }

    private AuditEventDto event(String action, String entityId, String userId,
                                Map<String, Object> metadata) {
        return new AuditEventDto(
                UUID.randomUUID().toString(),
                Instant.now(),
                action,
                entityId,
                userId,
                metadata);
    }

    private Map<String, Object> auditMetadata(Unit unit) {
        Map<String, Object> m = new HashMap<>();
        m.put("entityType", "Unit");
        m.put("code", unit.getCode());
        m.put("name", unit.getName());
        m.put("type", unit.getType());
        m.put("rate", unit.getRate() != null ? unit.getRate().toString() : null);
        m.put("available", unit.isAvailable());
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
