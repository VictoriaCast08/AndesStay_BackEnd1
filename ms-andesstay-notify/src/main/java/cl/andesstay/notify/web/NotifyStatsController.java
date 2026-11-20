package cl.duoc.andesstay.notify.web;

import cl.duoc.andesstay.notify.config.AndesStayRabbitProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotifyStatsController {

    private final AndesStayRabbitProperties rabbitProperties;

    public NotifyStatsController(AndesStayRabbitProperties rabbitProperties) {
        this.rabbitProperties = rabbitProperties;
    }

    @GetMapping("/internal/notify/stats")
    public Map<String, String> stats() {
        Map<String, String> stats = new LinkedHashMap<>();
        stats.put("service", "ms-andesstay-notify");
        stats.put("cmdExchange", rabbitProperties.getExchanges().getCmd());
        stats.put("topicExchange", rabbitProperties.getExchanges().getTopic());
        stats.put("dlxExchange", rabbitProperties.getExchanges().getDlx());
        stats.put("emailQueue", rabbitProperties.getQueues().getEmail());
        stats.put("ticketsQueue", rabbitProperties.getQueues().getTickets());
        stats.put("documentsQueue", rabbitProperties.getQueues().getDocuments());
        stats.put("reservationsQueue", rabbitProperties.getQueues().getReservations());
        return stats;
    }
}
