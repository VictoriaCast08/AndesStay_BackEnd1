package cl.duoc.andesstay.reservations.messaging;

public class ReservationEventMessage {

    private String event;
    private String reservationCode;
    private String guestName;
    private String status;
    private Long reservationId;
    private Long unitId;

    public ReservationEventMessage() {
    }

    public ReservationEventMessage(String event, String reservationCode, String guestName, String status,
                                   Long reservationId, Long unitId) {
        this.event = event;
        this.reservationCode = reservationCode;
        this.guestName = guestName;
        this.status = status;
        this.reservationId = reservationId;
        this.unitId = unitId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getReservationCode() {
        return reservationCode;
    }

    public void setReservationCode(String reservationCode) {
        this.reservationCode = reservationCode;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }
}
