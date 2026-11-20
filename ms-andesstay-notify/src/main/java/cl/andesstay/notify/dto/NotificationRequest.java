package cl.duoc.andesstay.notify.dto;

import jakarta.validation.constraints.NotBlank;

// DTO para representar el payload de las notificaciones
public class NotificationRequest {

    @NotBlank(message = "El tipo de notificacion es obligatorio")
    private String type; // email, housekeeping, voucher

    @NotBlank(message = "El mensaje no puede estar vacio")
    private String message;

    private String recipient;

    public NotificationRequest() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
}