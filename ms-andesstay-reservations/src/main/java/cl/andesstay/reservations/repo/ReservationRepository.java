package cl.duoc.andesstay.reservations.repo;

import cl.duoc.andesstay.reservations.domain.Reservation;
import cl.duoc.andesstay.reservations.domain.ReservationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Repositorio JPA para la entidad Reservation en PostgreSQL
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByCode(String code);
    List<Reservation> findByStatus(ReservationStatus status);
}