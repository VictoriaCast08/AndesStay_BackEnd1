package cl.duoc.andesstay.reservations.web;

import cl.duoc.andesstay.reservations.domain.Reservation;
import cl.duoc.andesstay.reservations.domain.ReservationStatus;
import cl.duoc.andesstay.reservations.repo.ReservationRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// Controlador REST para la gestion de reservas y envio de eventos a RabbitMQ
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationRepository repository;
    private final RabbitTemplate rabbitTemplate;

    // Nombre del Exchange y Routing Key para notificaciones
    private static final String NOTIFICATION_EXCHANGE = "andesstay.notify.exchange";
    private static final String NOTIFICATION_ROUTING_KEY = "notification.general";

    public ReservationController(ReservationRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
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
        publishNotificationEvent("RESERVATION_CREATED", saved);

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
        publishNotificationEvent("RESERVATION_STATUS_CHANGED", updated);

        return updated;
    }

    // Metodo auxiliar para publicar mensajes en el bus de RabbitMQ
    private void publishNotificationEvent(String eventType, Reservation reservation) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("event", eventType);
            message.put("reservationCode", reservation.getCode());
            message.put("guestName", reservation.getGuestName());
            message.put("status", reservation.getStatus().name());

            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, NOTIFICATION_ROUTING_KEY, message);
        } catch (Exception e) {
            // Log de advertencia sin bloquear la transaccion de la reserva
            System.err.println("No se pudo enviar la notificacion a RabbitMQ: " + e.getMessage());
        }
    }
}