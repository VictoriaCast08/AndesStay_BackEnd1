package cl.duoc.andesstay.notify.service;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Esqueleto: colas del caso email / housekeeping / voucher via RabbitMQ.
 * En EP1 se registra el envelope; la publicacion AMQP se activa con el broker.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final ConcurrentLinkedQueue<Map<String, Object>> queue = new ConcurrentLinkedQueue<>();
    private final AtomicLong sequence = new AtomicLong();

    public String enqueue(Map<String, Object> payload) {
        String ticket = "NTF-" + sequence.incrementAndGet();
        queue.add(payload);
        log.info("Enqueue notification {} type={}", ticket, payload.get("type"));
        return ticket;
    }

    public Map<String, Object> stats() {
        return Map.of(
                "service", "ms-andesstay-notify",
                "queueSize", queue.size(),
                "topology", Map.of(
                        "q.cmd.email", "email/push huesped",
                        "q.cmd.housekeeping", "ticket limpieza",
                        "q.cmd.voucher", "PDF voucher"));
    }
}
