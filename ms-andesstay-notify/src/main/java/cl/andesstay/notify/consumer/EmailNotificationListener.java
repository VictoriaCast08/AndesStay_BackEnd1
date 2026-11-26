package cl.andesstay.notify.consumer;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// Consumidor asíncrono para notificaciones de email
@Component
public class EmailNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationListener.class);

    // Escucha la cola principal de emails
    @RabbitListener(queues = "${andesstay.rabbitmq.queues.email}")
    public void processEmailNotification(Map<String, Object> payload, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("Consumiendo notificacion de EMAIL: {}", payload);

            if (payload == null || payload.isEmpty()) {
                throw new IllegalArgumentException("Payload de email invalido");
            }

            // Simulación de fallo para pruebas de DLQ
            if (payload.get("message") != null && payload.get("message").toString().contains("ERROR")) {
                throw new IllegalArgumentException("Error simulado para probar envio a DLQ");
            }

            // Confirmación explícita exitosa (ACK)
            channel.basicAck(deliveryTag, false);
            log.info("Notificacion de EMAIL procesada exitosamente con ACK.");

        } catch (MessageConversionException e) {
            log.error("JSON invalido en mensaje de EMAIL. DeliveryTag={}, MessageId={}. Redirigiendo a DLQ sin requeue.",
                    deliveryTag, message.getMessageProperties().getMessageId(), e);
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            log.error("Error al procesar notificacion de EMAIL. DeliveryTag={}, MessageId={}. Redirigiendo a DLQ sin requeue. Causa: {}",
                    deliveryTag, message.getMessageProperties().getMessageId(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
