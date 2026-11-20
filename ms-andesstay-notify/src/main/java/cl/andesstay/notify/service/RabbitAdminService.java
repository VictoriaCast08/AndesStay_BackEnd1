package cl.duoc.andesstay.notify.service;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;

// Servicio de administración dinámica de infraestructura en RabbitMQ
@Service
public class RabbitAdminService {

    private final AmqpAdmin amqpAdmin;

    public RabbitAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    // Crea colas Quorum en el clúster
    public void createQueue(String name, boolean durable) {
        Queue queue = durable
                ? QueueBuilder.durable(name).quorum().build()
                : QueueBuilder.nonDurable(name).build();
        amqpAdmin.declareQueue(queue);
    }

    // Elimina una cola existente
    public boolean deleteQueue(String name) {
        return amqpAdmin.deleteQueue(name);
    }

    // Crea un exchange tipo Direct
    public void createExchange(String name) {
        createExchange(name, "direct", true);
    }

    public void createExchange(String name, String type, boolean durable) {
        String normalized = type == null ? "direct" : type.toLowerCase();
        Exchange exchange = switch (normalized) {
            case "topic" -> new TopicExchange(name, durable, false);
            case "fanout" -> new FanoutExchange(name, durable, false);
            default -> new DirectExchange(name, durable, false);
        };
        amqpAdmin.declareExchange(exchange);
    }

    // Elimina un exchange existente
    public boolean deleteExchange(String name) {
        return amqpAdmin.deleteExchange(name);
    }
}