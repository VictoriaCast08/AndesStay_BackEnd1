package cl.andesstay.reservations.admin;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

// Servicio que encapsula RabbitAdmin para administracion de colas, exchanges y bindings via REST
@Service
public class RabbitAdminService {

    private final RabbitAdmin rabbitAdmin;

    public RabbitAdminService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    // --- COLAS ---

    public void createQueue(String queueName) {
        Queue queue = new Queue(queueName, true, false, false);
        rabbitAdmin.declareQueue(queue);
    }

    public void deleteQueue(String queueName) {
        rabbitAdmin.deleteQueue(queueName);
    }

    // --- EXCHANGES ---

    public void createExchange(String name, String type, boolean durable, boolean autoDelete) {
        Exchange exchange = buildExchange(name, type, durable, autoDelete);
        rabbitAdmin.declareExchange(exchange);
    }

    public void deleteExchange(String name) {
        rabbitAdmin.deleteExchange(name);
    }

    // --- BINDINGS ---

    public void createBinding(String queueName, String exchangeName, String routingKey) {
        // Asegurar que la cola existe
        rabbitAdmin.declareQueue(new Queue(queueName, true, false, false));
        // Asegurar que el exchange existe (tipo direct por defecto para bindings administrativos)
        rabbitAdmin.declareExchange(new DirectExchange(exchangeName, true, false));
        Binding binding = BindingBuilder.bind(new Queue(queueName, true))
                .to(new DirectExchange(exchangeName, true, false))
                .with(routingKey);
        rabbitAdmin.declareBinding(binding);
    }

    public void deleteBinding(String queueName, String exchangeName, String routingKey) {
        Binding binding = BindingBuilder.bind(new Queue(queueName, true))
                .to(new DirectExchange(exchangeName, true, false))
                .with(routingKey);
        rabbitAdmin.removeBinding(binding);
    }

    // --- HELPERS ---

    private Exchange buildExchange(String name, String type, boolean durable, boolean autoDelete) {
        return switch (type.toLowerCase()) {
            case "direct" -> new DirectExchange(name, durable, autoDelete);
            case "topic" -> new TopicExchange(name, durable, autoDelete);
            case "fanout" -> new FanoutExchange(name, durable, autoDelete);
            case "headers" -> new HeadersExchange(name, durable, autoDelete);
            default -> throw new IllegalArgumentException("Tipo de exchange no soportado: " + type);
        };
    }
}