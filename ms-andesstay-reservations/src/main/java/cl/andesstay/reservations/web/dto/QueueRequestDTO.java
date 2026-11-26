package cl.andesstay.reservations.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// DTO validado para peticiones de creacion de colas RabbitMQ
public class QueueRequestDTO {

    @NotBlank(message = "El nombre de la cola es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
             message = "El nombre de la cola solo puede contener letras, numeros, punto, guion bajo y guion")
    private String name;

    public QueueRequestDTO() {
    }

    public QueueRequestDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}