package cl.andesstay.report.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuración programática de los tópicos Kafka de analítica de reportes.
 * Se crean automáticamente al iniciar el microservicio si no existen.
 */
@Configuration
public class KafkaTopicConfig {

    public static final String TOPIC_ANALYTICS_RESERVATIONS = "andesstay.analytics.reservations";
    public static final String TOPIC_ANALYTICS_RESERVATIONS_DLT = "andesstay.analytics.reservations.dlt";
    public static final String TOPIC_AUDIT_EVENTS = "andesstay.audit.events";
    public static final String TOPIC_AUDIT_EVENTS_DLT = "andesstay.audit.events.dlt";

    @Bean
    public NewTopic andesstayAnalyticsReservations() {
        return TopicBuilder.name(TOPIC_ANALYTICS_RESERVATIONS)
                .partitions(3)
                .replicas(3)
                .config("retention.ms", "604800000")
                .config("cleanup.policy", "delete")
                .build();
    }

    @Bean
    public NewTopic andesstayAnalyticsReservationsDlt() {
        return TopicBuilder.name(TOPIC_ANALYTICS_RESERVATIONS_DLT)
                .partitions(3)
                .replicas(3)
                .config("retention.ms", "604800000")
                .config("cleanup.policy", "delete")
                .build();
    }

    @Bean
    public NewTopic andesstayAuditEvents() {
        return TopicBuilder.name(TOPIC_AUDIT_EVENTS)
                .partitions(3)
                .replicas(3)
                .config("retention.ms", "2592000000")
                .config("cleanup.policy", "delete")
                .build();
    }

    @Bean
    public NewTopic andesstayAuditEventsDlt() {
        return TopicBuilder.name(TOPIC_AUDIT_EVENTS_DLT)
                .partitions(3)
                .replicas(3)
                .config("retention.ms", "2592000000")
                .config("cleanup.policy", "delete")
                .build();
    }
}
