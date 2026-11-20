package cl.duoc.andesstay.reservations.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

// Entidad JPA para la gestion de reservas en PostgreSQL
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private Long unitId;

    @Column(nullable = false)
    private String guestName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.CREADA;

    private LocalDate stayFrom;
    private LocalDate stayTo;

    public Reservation() {
    }

    public Reservation(String code, Long unitId, String guestName, LocalDate stayFrom, LocalDate stayTo) {
        this.code = code;
        this.unitId = unitId;
        this.guestName = guestName;
        this.stayFrom = stayFrom;
        this.stayTo = stayTo;
        this.status = ReservationStatus.CREADA;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public LocalDate getStayFrom() { return stayFrom; }
    public void setStayFrom(LocalDate stayFrom) { this.stayFrom = stayFrom; }

    public LocalDate getStayTo() { return stayTo; }
    public void setStayTo(LocalDate stayTo) { this.stayTo = stayTo; }
}