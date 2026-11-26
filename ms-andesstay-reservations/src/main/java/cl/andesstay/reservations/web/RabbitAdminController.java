package cl.andesstay.reservations.web;

import cl.andesstay.reservations.admin.RabbitAdminService;
import cl.andesstay.reservations.web.dto.BindingRequestDTO;
import cl.andesstay.reservations.web.dto.ExchangeRequestDTO;
import cl.andesstay.reservations.web.dto.QueueRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controlador REST para administracion de colas, exchanges y bindings RabbitMQ
@RestController
@RequestMapping("/api/rabbitmq/admin")
public class RabbitAdminController {

    private final RabbitAdminService rabbitAdminService;

    public RabbitAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    // --- COLAS ---

    // Crear una nueva cola en RabbitMQ
    @PostMapping("/queues")
    public ResponseEntity<Void> createQueue(@Valid @RequestBody QueueRequestDTO dto) {
        rabbitAdminService.createQueue(dto.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Eliminar una cola existente de RabbitMQ
    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> deleteQueue(@PathVariable String name) {
        rabbitAdminService.deleteQueue(name);
        return ResponseEntity.noContent().build();
    }

    // --- EXCHANGES ---

    // Crear un nuevo exchange en RabbitMQ
    @PostMapping("/exchanges")
    public ResponseEntity<Void> createExchange(@Valid @RequestBody ExchangeRequestDTO dto) {
        rabbitAdminService.createExchange(dto.getName(), dto.getType(), dto.isDurable(), dto.isAutoDelete());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Eliminar un exchange existente de RabbitMQ
    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> deleteExchange(@PathVariable String name) {
        rabbitAdminService.deleteExchange(name);
        return ResponseEntity.noContent().build();
    }

    // --- BINDINGS ---

    // Crear un binding entre una cola y un exchange en RabbitMQ
    @PostMapping("/bindings")
    public ResponseEntity<Void> createBinding(@Valid @RequestBody BindingRequestDTO dto) {
        rabbitAdminService.createBinding(dto.getQueueName(), dto.getExchangeName(), dto.getRoutingKey());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Eliminar un binding existente de RabbitMQ
    @DeleteMapping("/bindings")
    public ResponseEntity<Void> deleteBinding(@Valid @RequestBody BindingRequestDTO dto) {
        rabbitAdminService.deleteBinding(dto.getQueueName(), dto.getExchangeName(), dto.getRoutingKey());
        return ResponseEntity.noContent().build();
    }
}