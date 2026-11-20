package cl.duoc.andesstay.notify.consumer;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReservationEventListener.class);

    @RabbitListener(queues = "${andesstay.rabbitmq.queues.reservations}")
    public void processReservationEvent(Map<String, Object> payload, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("Consumiendo evento de reserva: {}", payload);
            if (payload == null
                    || payload.get("event") == null
                    || payload.get("reservationCode") == null
                    || payload.get("event").toString().isBlank()
                    || payload.get("reservationCode").toString().isBlank()) {
                throw new IllegalArgumentException("Payload de evento de reserva invalido");
            }
            if (payload.get("message") != null && payload.get("message").toString().contains("ERROR")) {
                throw new IllegalArgumentException("Error de negocio al procesar evento de reserva");
            }
            channel.basicAck(deliveryTag, false);
            log.info("Evento de reserva confirmado con ACK.");
        } catch (MessageConversionException e) {
            log.error("JSON invalido en mensaje de RESERVA. DeliveryTag={}, MessageId={}. Redirigiendo a DLQ sin requeue.",
                    deliveryTag, message.getMessageProperties().getMessageId(), e);
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            log.error("Error al procesar evento de reserva. DeliveryTag={}, MessageId={}. Redirigiendo a DLQ sin requeue. Causa: {}",
                    deliveryTag, message.getMessageProperties().getMessageId(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
