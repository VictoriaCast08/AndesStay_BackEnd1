package cl.duoc.andesstay.reservations.web.dto;

import jakarta.validation.constraints.NotBlank;

// DTO validado para peticiones de creacion/eliminacion de bindings RabbitMQ
public class BindingRequestDTO {

    @NotBlank(message = "El nombre de la cola es obligatorio")
    private String queueName;

    @NotBlank(message = "El nombre del exchange es obligatorio")
    private String exchangeName;

    @NotBlank(message = "La routing key es obligatoria")
    private String routingKey;

    public BindingRequestDTO() {
    }

    public BindingRequestDTO(String queueName, String exchangeName, String routingKey) {
        this.queueName = queueName;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
}