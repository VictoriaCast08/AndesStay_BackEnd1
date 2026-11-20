package cl.duoc.andesstay.reservations.web;

import cl.duoc.andesstay.reservations.domain.Reservation;
import cl.duoc.andesstay.reservations.domain.ReservationStatus;
import cl.duoc.andesstay.reservations.messaging.ReservationMessagePublisher;
import cl.duoc.andesstay.reservations.repo.ReservationRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// Controlador REST para la gestion de reservas y envio de eventos a RabbitMQ
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationRepository repository;
    private final ReservationMessagePublisher publisher;

    public ReservationController(ReservationRepository repository, ReservationMessagePublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
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

    // Crear reserva y publicar evento en RabbitMQ para notificar al cliente
    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    public Reservation create(@RequestBody Reservation body) {
        body.setId(null);
        if (body.getStatus() == null) {
            body.setStatus(ReservationStatus.CREADA);
        }

        Reservation saved = repository.save(body);

        // Enviar evento de notificacion a RabbitMQ
        publisher.publishCreated(saved);

        return saved;
    }

    // Cambiar estado de la reserva y notificar el cambio
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('Admin','Operador')")
    public Reservation changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        ReservationStatus next = ReservationStatus.valueOf(body.get("status"));

        if (next == ReservationStatus.CHECKIN_PENDIENTE && reservation.getStatus() != ReservationStatus.CONFIRMADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede hacer check-in sin CONFIRMAR");
        }

        reservation.setStatus(next);
        Reservation updated = repository.save(reservation);

        // Enviar evento de cambio de estado a RabbitMQ
        publisher.publishStatusChanged(updated);

        return updated;
    }
}
