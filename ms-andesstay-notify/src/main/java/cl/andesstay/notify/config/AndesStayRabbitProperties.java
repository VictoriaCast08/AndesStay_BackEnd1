package cl.duoc.andesstay.notify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "andesstay.rabbitmq")
public class AndesStayRabbitProperties {

    private Exchanges exchanges = new Exchanges();
    private Queues queues = new Queues();
    private RoutingKeys routingKeys = new RoutingKeys();

    public Exchanges getExchanges() {
        return exchanges;
    }

    public void setExchanges(Exchanges exchanges) {
        this.exchanges = exchanges;
    }

    public Queues getQueues() {
        return queues;
    }

    public void setQueues(Queues queues) {
        this.queues = queues;
    }

    public RoutingKeys getRoutingKeys() {
        return routingKeys;
    }

    public void setRoutingKeys(RoutingKeys routingKeys) {
        this.routingKeys = routingKeys;
    }

    public static class Exchanges {
        private String cmd;
        private String topic;
        private String dlx;

        public String getCmd() {
            return cmd;
        }

        public void setCmd(String cmd) {
            this.cmd = cmd;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getDlx() {
            return dlx;
        }

        public void setDlx(String dlx) {
            this.dlx = dlx;
        }
    }

    public static class Queues {
        private String email;
        private String emailDlq;
        private String tickets;
        private String ticketsDlq;
        private String documents;
        private String documentsDlq;
        private String reservations;
        private String reservationsDlq;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getEmailDlq() {
            return emailDlq;
        }

        public void setEmailDlq(String emailDlq) {
            this.emailDlq = emailDlq;
        }

        public String getTickets() {
            return tickets;
        }

        public void setTickets(String tickets) {
            this.tickets = tickets;
        }

        public String getTicketsDlq() {
            return ticketsDlq;
        }

        public void setTicketsDlq(String ticketsDlq) {
            this.ticketsDlq = ticketsDlq;
        }

        public String getDocuments() {
            return documents;
        }

        public void setDocuments(String documents) {
            this.documents = documents;
        }

        public String getDocumentsDlq() {
            return documentsDlq;
        }

        public void setDocumentsDlq(String documentsDlq) {
            this.documentsDlq = documentsDlq;
        }

        public String getReservations() {
            return reservations;
        }

        public void setReservations(String reservations) {
            this.reservations = reservations;
        }

        public String getReservationsDlq() {
            return reservationsDlq;
        }

        public void setReservationsDlq(String reservationsDlq) {
            this.reservationsDlq = reservationsDlq;
        }
    }

    public static class RoutingKeys {
        private String email;
        private String tickets;
        private String documents;
        private String emailDlq;
        private String ticketsDlq;
        private String documentsDlq;
        private String reservations;
        private String reservationsDlq;
        private String reservationCreated;
        private String reservationUpdated;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getTickets() {
            return tickets;
        }

        public void setTickets(String tickets) {
            this.tickets = tickets;
        }

        public String getTicketsDlq() {
            return ticketsDlq;
        }

        public void setTicketsDlq(String ticketsDlq) {
            this.ticketsDlq = ticketsDlq;
        }

        public String getDocuments() {
            return documents;
        }

        public void setDocuments(String documents) {
            this.documents = documents;
        }

        public String getDocumentsDlq() {
            return documentsDlq;
        }

        public void setDocumentsDlq(String documentsDlq) {
            this.documentsDlq = documentsDlq;
        }

        public String getEmailDlq() {
            return emailDlq;
        }

        public void setEmailDlq(String emailDlq) {
            this.emailDlq = emailDlq;
        }

        public String getReservations() {
            return reservations;
        }

        public void setReservations(String reservations) {
            this.reservations = reservations;
        }

        public String getReservationsDlq() {
            return reservationsDlq;
        }

        public void setReservationsDlq(String reservationsDlq) {
            this.reservationsDlq = reservationsDlq;
        }

        public String getReservationCreated() {
            return reservationCreated;
        }

        public void setReservationCreated(String reservationCreated) {
            this.reservationCreated = reservationCreated;
        }

        public String getReservationUpdated() {
            return reservationUpdated;
        }

        public void setReservationUpdated(String reservationUpdated) {
            this.reservationUpdated = reservationUpdated;
        }
    }
}
