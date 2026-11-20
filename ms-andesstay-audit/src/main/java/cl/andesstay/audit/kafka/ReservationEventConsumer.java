package cl.duoc.andesstay.audit.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Esqueleto consumidor Kafka (topico reservations.events).
 * Se conecta con spring-kafka cuando KAFKA_BOOTSTRAP este disponible.
 */
@Component
public class ReservationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReservationEventConsumer.class);

    public void onEvent(String payload) {
        log.info("Audit consumer received: {}", payload);
    }
}
