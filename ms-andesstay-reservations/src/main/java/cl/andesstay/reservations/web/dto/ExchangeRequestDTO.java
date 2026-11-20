package cl.duoc.andesstay.reservations.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// DTO validado para peticiones de creacion de exchanges RabbitMQ
public class ExchangeRequestDTO {

    @NotBlank(message = "El nombre del exchange es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
             message = "El nombre del exchange solo puede contener letras, numeros, punto, guion bajo y guion")
    private String name;

    @NotBlank(message = "El tipo del exchange es obligatorio")
    @Pattern(regexp = "^(direct|topic|fanout|headers)$",
             message = "El tipo del exchange debe ser: direct, topic, fanout o headers")
    private String type;

    private boolean durable = true;
    private boolean autoDelete = false;

    public ExchangeRequestDTO() {
    }

    public ExchangeRequestDTO(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }
}