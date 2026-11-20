package cl.duoc.andesstay.notify.dto;

import jakarta.validation.constraints.NotBlank;

// DTO para la creación dinámica de colas vía REST
public class CreateQueueRequest {

    @NotBlank(message = "El nombre de la cola es obligatorio")
    private String name;

    private boolean durable = true;

    public CreateQueueRequest() {}

    public CreateQueueRequest(String name, boolean durable) {
        this.name = name;
        this.durable = durable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }
}