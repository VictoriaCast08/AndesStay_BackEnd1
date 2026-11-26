package cl.andesstay.reservations.web;

import cl.andesstay.reservations.domain.Reservation;
import cl.andesstay.reservations.domain.ReservationStatus;
import cl.andesstay.reservations.messaging.ReservationMessagePublisher;
import cl.andesstay.reservations.repo.ReservationRepository;
import cl.andesstay.reservations.kafka.KafkaEventProducerService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// Controlador REST para la gestion de reservas y envio de eventos a RabbitMQ y Kafka
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationRepository repository;
    private final ReservationMessagePublisher publisher;
    private final KafkaEventProducerService kafkaProducer;

    public ReservationController(ReservationRepository repository,
                                 ReservationMessagePublisher publisher,
                                 KafkaEventProducerService kafkaProducer) {
        this.repository = repository;
        this.publisher = publisher;
        this.kafkaProducer = kafkaProducer;
    }

    // Listar reservas con filtro opcional por estado
    @GetMapping
    public List<Reservation> list(@RequestParam(required = false) ReservationStatus status) {
        if (status != null) {
            return repository.findByStatus(status);
        }
        return repository.findAll();
    }

    // Obtener reserva por ID
    @GetMapping("/{id}")
    public Reservation get(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));
    }

    // Crear reserva y publicar evento en RabbitMQ y Kafka (analítica + auditoría)
    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    public Reservation create(@RequestBody Reservation body, Authentication authentication) {
        body.setId(null);
        if (body.getStatus() == null) {
            body.setStatus(ReservationStatus.CREADA);
        }

        Reservation saved = repository.save(body);

        // Enviar evento de notificacion a RabbitMQ (sin alterar la lógica existente)
        publisher.publishCreated(saved);

        // Publicar evento en Kafka (analítica y auditoría) en paralelo a RabbitMQ
        String userId = authentication != null ? authentication.getName() : saved.getGuestName();
        kafkaProducer.publishReservationCreated(saved, userId);

        return saved;
    }

    // Cambiar estado de la reserva y notificar el cambio (RabbitMQ + Kafka)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('Admin','Operador')")
    public Reservation changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body,
                                    Authentication authentication) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        ReservationStatus next = ReservationStatus.valueOf(body.get("status"));

        if (next == ReservationStatus.CHECKIN_PENDIENTE && reservation.getStatus() != ReservationStatus.CONFIRMADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede hacer check-in sin CONFIRMAR");
        }

        reservation.setStatus(next);
        Reservation updated = repository.save(reservation);

        // Enviar evento de cambio de estado a RabbitMQ (sin alterar la lógica existente)
        publisher.publishStatusChanged(updated);

        // Publicar evento en Kafka al confirmar o cancelar (auditoría + analítica)
        String userId = authentication != null ? authentication.getName() : updated.getGuestName();
        switch (next) {
            case CONFIRMADA -> kafkaProducer.publishReservationConfirmed(updated, userId);
            case CANCELADA -> kafkaProducer.publishReservationCancelled(updated, userId);
            default -> {
                // Otros estados mantienen RabbitMQ; Kafka solo se dispara en crear/confirmar/cancelar
            }
        }

        return updated;
    }
}
