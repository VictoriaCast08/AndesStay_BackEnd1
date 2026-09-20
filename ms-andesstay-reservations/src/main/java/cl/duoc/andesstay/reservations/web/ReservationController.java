package cl.duoc.andesstay.reservations.web;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import cl.duoc.andesstay.reservations.domain.Reservation;
import cl.duoc.andesstay.reservations.domain.ReservationStatus;
import cl.duoc.andesstay.reservations.repo.ReservationRepository;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationRepository repository;

    public ReservationController(ReservationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Reservation> list(@RequestParam(required = false) ReservationStatus status) {
        if (status != null) {
            return repository.findByStatus(status);
        }
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Reservation get(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    public Reservation create(@RequestBody Reservation body) {
        body.setId(null);
        if (body.getStatus() == null) {
            body.setStatus(ReservationStatus.CREADA);
        }
        return repository.save(body);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('Admin','Operador')")
    public Reservation changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));
        ReservationStatus next = ReservationStatus.valueOf(body.get("status"));
        if (next == ReservationStatus.CHECKIN_PENDIENTE
                && reservation.getStatus() != ReservationStatus.CONFIRMADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede hacer check-in sin CONFIRMAR");
        }
        reservation.setStatus(next);
        return repository.save(reservation);
    }
}
