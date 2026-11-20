package cl.duoc.andesstay.notify.web;

import cl.duoc.andesstay.notify.dto.NotificationRequest;
import cl.duoc.andesstay.notify.service.NotificationService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controlador para encolar notificaciones
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Procesa y encola la notificacion retornando un ticket
    @PostMapping
    public ResponseEntity<Map<String, String>> sendNotification(@Valid @RequestBody NotificationRequest request) {
        String ticket = notificationService.enqueue(request);
        return ResponseEntity.ok(Map.of(
                "status", "QUEUED",
                "ticket", ticket,
                "message", "Notificacion procesada exitosamente"
        ));
    }
}