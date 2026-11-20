package cl.duoc.andesstay.notify.service;

import cl.duoc.andesstay.notify.config.AndesStayRabbitProperties;
import cl.duoc.andesstay.notify.dto.NotificationRequest;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

// Servicio para el procesamiento y simulación de envío de notificaciones
@Service
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;
    private final AndesStayRabbitProperties rabbitProperties;
    private final AtomicLong sequence = new AtomicLong();

    public NotificationService(RabbitTemplate rabbitTemplate, AndesStayRabbitProperties rabbitProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitProperties = rabbitProperties;
    }

    public String enqueue(NotificationRequest request) {
        String ticket = "NTF-" + sequence.incrementAndGet();

        // Determina la Routing Key según el tipo de notificación
        String routingKey = switch (request.getType().toLowerCase()) {
            case "ticket", "tickets" -> rabbitProperties.getRoutingKeys().getTickets();
            case "document", "documents" -> rabbitProperties.getRoutingKeys().getDocuments();
            case "housekeeping", "voucher", "email" -> rabbitProperties.getRoutingKeys().getEmail();
            default -> rabbitProperties.getRoutingKeys().getEmail();
        };

        Map<String, Object> payload = Map.of(
                "ticket", ticket,
                "type", request.getType(),
                "message", request.getMessage(),
                "recipient", request.getRecipient() != null ? request.getRecipient() : "N/A",
                "reservationCode", ticket
        );

        // Envía directamente al exchange genérico de comandos
        rabbitTemplate.convertAndSend(rabbitProperties.getExchanges().getCmd(), routingKey, payload);

        return ticket;
    }
}
