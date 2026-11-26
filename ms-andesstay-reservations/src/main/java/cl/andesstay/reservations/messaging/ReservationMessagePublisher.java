package cl.andesstay.reservations.messaging;

import cl.andesstay.reservations.config.AndesStayRabbitProperties;
import cl.andesstay.reservations.domain.Reservation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReservationMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final AndesStayRabbitProperties rabbitProperties;

    public ReservationMessagePublisher(RabbitTemplate rabbitTemplate, AndesStayRabbitProperties rabbitProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitProperties = rabbitProperties;
    }

    // Enviar evento de notificacion a RabbitMQ
    public void publishCreated(Reservation reservation) {
        publish("RESERVATION_CREATED", rabbitProperties.getRoutingKeys().getReservationCreated(), reservation);
    }

    // Enviar evento de cambio de estado a RabbitMQ
    public void publishStatusChanged(Reservation reservation) {
        publish("RESERVATION_STATUS_CHANGED", rabbitProperties.getRoutingKeys().getReservationUpdated(), reservation);
    }

    // Metodo auxiliar para publicar mensajes en el bus de RabbitMQ
    private void publish(String eventType, String eventRoutingKey, Reservation reservation) {
        try {
            ReservationEventMessage event = new ReservationEventMessage(
                    eventType,
                    reservation.getCode(),
                    reservation.getGuestName(),
                    reservation.getStatus().name(),
                    reservation.getId(),
                    reservation.getUnitId());
            rabbitTemplate.convertAndSend(
                    rabbitProperties.getExchanges().getTopic(),
                    eventRoutingKey,
                    event);
            rabbitTemplate.convertAndSend(
                    rabbitProperties.getExchanges().getCmd(),
                    rabbitProperties.getRoutingKeys().getEmail(),
                    command(eventType, "email", reservation));
            rabbitTemplate.convertAndSend(
                    rabbitProperties.getExchanges().getCmd(),
                    rabbitProperties.getRoutingKeys().getTickets(),
                    command(eventType, "tickets", reservation));
            rabbitTemplate.convertAndSend(
                    rabbitProperties.getExchanges().getCmd(),
                    rabbitProperties.getRoutingKeys().getDocuments(),
                    command(eventType, "documents", reservation));
        } catch (Exception e) {
            // Log de advertencia sin bloquear la transaccion de la reserva
            System.err.println("No se pudo enviar la notificacion a RabbitMQ: " + e.getMessage());
        }
    }

    private ReservationCommandMessage command(String eventType, String type, Reservation reservation) {
        ReservationCommandMessage command = new ReservationCommandMessage();
        command.setEvent(eventType);
        command.setType(type);
        command.setReservationCode(reservation.getCode());
        command.setGuestName(reservation.getGuestName());
        command.setStatus(reservation.getStatus().name());
        command.setReservationId(reservation.getId());
        command.setRecipient(reservation.getGuestName());
        command.setMessage(eventType + " " + reservation.getCode() + " " + reservation.getStatus().name());
        return command;
    }
}
