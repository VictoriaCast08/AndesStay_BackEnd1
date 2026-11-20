package cl.duoc.andesstay.reservations.domain;

// Enum que define el ciclo de vida de una reserva en AndesStay
public enum ReservationStatus {
    CREADA,
    CONFIRMADA,
    CHECKIN_PENDIENTE,
    EN_ESTADIA,
    CHECKOUT,
    CANCELADA
}