package cl.duoc.andesstay.notify.web;

import java.util.Map;
import org.springframework.web.bind.annotation.*;
import cl.duoc.andesstay.notify.service.NotificationService;

/**
 * Health interno. El envio real es consumidor RabbitMQ (no publico tras Gateway).
 */
@RestController
@RequestMapping("/internal/notify")
public class NotifyController {

    private final NotificationService service;

    public NotifyController(NotificationService service) {
        this.service = service;
    }

    @PostMapping("/enqueue")
    public Map<String, String> enqueue(@RequestBody Map<String, Object> payload) {
        String ticket = service.enqueue(payload);
        return Map.of("status", "queued", "ticket", ticket);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return service.stats();
    }
}
