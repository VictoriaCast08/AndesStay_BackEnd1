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

@Component
public class DocumentListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentListener.class);

    @RabbitListener(queues = "${andesstay.rabbitmq.queues.documents}")
    public void processDocument(Map<String, Object> payload, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("Consumiendo documento: {}", payload);
            if (payload == null || payload.get("reservationCode") == null || payload.get("reservationCode").toString().isBlank()) {
                throw new IllegalArgumentException("Payload de documento invalido");
            }
            if (payload.get("message") != null && payload.get("message").toString().contains("ERROR")) {
                throw new IllegalArgumentException("Error de negocio al generar documento");
            }
            channel.basicAck(deliveryTag, false);
            log.info("Documento confirmado con ACK.");
        } catch (MessageConversionException e) {
            log.error("JSON invalido en mensaje de DOCUMENTO. DeliveryTag={}, MessageId={}. Redirigiendo a DLQ sin requeue.",
                    deliveryTag, message.getMessageProperties().getMessageId(), e);
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            log.error("Error al procesar documento. DeliveryTag={}, MessageId={}. Redirigiendo a DLQ sin requeue. Causa: {}",
                    deliveryTag, message.getMessageProperties().getMessageId(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
