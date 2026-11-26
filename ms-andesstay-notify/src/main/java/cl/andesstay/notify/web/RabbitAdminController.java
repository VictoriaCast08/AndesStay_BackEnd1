package cl.andesstay.notify.web;

import cl.andesstay.notify.dto.CreateExchangeRequest;
import cl.andesstay.notify.dto.CreateQueueRequest;
import cl.andesstay.notify.service.RabbitAdminService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controlador para administracion dinamica de RabbitMQ
@RestController
@RequestMapping("/api/admin/rabbit")
public class RabbitAdminController {

    private final RabbitAdminService rabbitAdminService;

    public RabbitAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    // Crea una nueva cola Quorum
    @PostMapping("/queues")
    public ResponseEntity<Map<String, String>> createQueue(@Valid @RequestBody CreateQueueRequest request) {
        rabbitAdminService.createQueue(request.getName(), request.isDurable());
        return ResponseEntity.ok(Map.of("message", "Cola creada exitosamente: " + request.getName()));
    }

    // Elimina una cola existente
    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Map<String, String>> deleteQueue(@PathVariable String name) {
        boolean deleted = rabbitAdminService.deleteQueue(name);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Cola eliminada: " + name));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "No se pudo eliminar la cola: " + name));
    }

    // Crea un exchange tipo Direct
    @PostMapping("/exchanges")
    public ResponseEntity<Map<String, String>> createExchange(@Valid @RequestBody CreateExchangeRequest request) {
        rabbitAdminService.createExchange(request.getName(), request.getType(), request.isDurable());
        return ResponseEntity.ok(Map.of("message", "Exchange creado exitosamente: " + request.getName()));
    }

    @PostMapping("/exchanges/{name}")
    public ResponseEntity<Map<String, String>> createExchangeByName(@PathVariable String name) {
        rabbitAdminService.createExchange(name);
        return ResponseEntity.ok(Map.of("message", "Exchange creado exitosamente: " + name));
    }

    // Elimina un exchange existente
    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Map<String, String>> deleteExchange(@PathVariable String name) {
        boolean deleted = rabbitAdminService.deleteExchange(name);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Exchange eliminado: " + name));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "No se pudo eliminar el exchange: " + name));
    }
}