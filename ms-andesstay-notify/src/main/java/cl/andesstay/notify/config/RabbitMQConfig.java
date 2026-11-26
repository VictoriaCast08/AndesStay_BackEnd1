package cl.andesstay.notify.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class RabbitMQConfig {

    // --- CONSTANTES DE TOPOLOGÍA ---
    // --- CONVERTER JSON ---
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitAdmin rabbitAdmin(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                                   Environment environment) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(!environment.matchesProfiles("local"));
        return rabbitAdmin;
    }

    // --- EXCHANGES ---
    @Bean
    public DirectExchange cmdDirectExchange(AndesStayRabbitProperties properties) {
        return new DirectExchange(properties.getExchanges().getCmd(), true, false);
    }

    @Bean
    public TopicExchange topicExchange(AndesStayRabbitProperties properties) {
        return new TopicExchange(properties.getExchanges().getTopic(), true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange(AndesStayRabbitProperties properties) {
        return new DirectExchange(properties.getExchanges().getDlx(), true, false);
    }

    // --- DEAD LETTER QUEUE (DLQ) ---
    @Bean
    public Queue emailDlq(AndesStayRabbitProperties properties) {
        return QueueBuilder.durable(properties.getQueues().getEmailDlq()).build();
    }

    @Bean
    public Queue ticketsDlq(AndesStayRabbitProperties properties) {
        return QueueBuilder.durable(properties.getQueues().getTicketsDlq()).build();
    }

    @Bean
    public Queue documentsDlq(AndesStayRabbitProperties properties) {
        return QueueBuilder.durable(properties.getQueues().getDocumentsDlq()).build();
    }

    @Bean
    public Queue reservationsDlq(AndesStayRabbitProperties properties) {
        return QueueBuilder.durable(properties.getQueues().getReservationsDlq()).build();
    }

    @Bean
    public Binding emailDlqBinding(Queue emailDlq, DirectExchange deadLetterExchange, AndesStayRabbitProperties properties) {
        return BindingBuilder.bind(emailDlq).to(deadLetterExchange).with(properties.getRoutingKeys().getEmailDlq());
    }

    @Bean
    public Binding ticketsDlqBinding(Queue ticketsDlq, DirectExchange deadLetterExchange, AndesStayRabbitProperties properties) {
        return BindingBuilder.bind(ticketsDlq).to(deadLetterExchange).with(properties.getRoutingKeys().getTicketsDlq());
    }

    @Bean
    public Binding documentsDlqBinding(Queue documentsDlq, DirectExchange deadLetterExchange, AndesStayRabbitProperties properties) {
        return BindingBuilder.bind(documentsDlq).to(deadLetterExchange).with(properties.getRoutingKeys().getDocumentsDlq());
    }

    @Bean
    public Binding reservationsDlqBinding(Queue reservationsDlq, DirectExchange deadLetterExchange, AndesStayRabbitProperties properties) {
        return BindingBuilder.bind(reservationsDlq).to(deadLetterExchange).with(properties.getRoutingKeys().getReservationsDlq());
    }

    // --- COLA PRINCIPAL (Conectada a DLQ) ---
    @Bean
    public Queue emailQueue(AndesStayRabbitProperties properties) {
        return QueueBuilder.durable(properties.getQueues().getEmail())
                .withArgument("x-dead-letter-exchange", properties.getExchanges().getDlx())
                .withArgument("x-dead-letter-routing-key", properties.getRoutingKeys().getEmailDlq())
                .build();
    }

    @Bean
    public Queue ticketsQueue(AndesStayRabbitProperties properties) {
        return QueueBuilder.durable(properties.getQueues().getTickets())
                .withArgument("x-dead-letter-exchange", properties.getExchanges().getDlx())
                .withArgument("x-dead-letter-routing-key", properties.getRoutingKeys().getTicketsDlq())
                .build();
    }

    @Bean
    public Queue documentsQueue(AndesStayRabbitProperties properties) {
        return QueueBuilder.durable(properties.getQueues().getDocuments())
                .withArgument("x-dead-letter-exchange", properties.getExchanges().getDlx())
                .withArgument("x-dead-letter-routing-key", properties.getRoutingKeys().getDocumentsDlq())
                .build();
    }

    @Bean
    public Queue reservationsQueue(AndesStayRabbitProperties properties) {
        return QueueBuilder.durable(properties.getQueues().getReservations())
                .withArgument("x-dead-letter-exchange", properties.getExchanges().getDlx())
                .withArgument("x-dead-letter-routing-key", properties.getRoutingKeys().getReservationsDlq())
                .build();
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange cmdDirectExchange, AndesStayRabbitProperties properties) {
        return BindingBuilder.bind(emailQueue).to(cmdDirectExchange).with(properties.getRoutingKeys().getEmail());
    }

    @Bean
    public Binding ticketsBinding(Queue ticketsQueue, DirectExchange cmdDirectExchange, AndesStayRabbitProperties properties) {
        return BindingBuilder.bind(ticketsQueue).to(cmdDirectExchange).with(properties.getRoutingKeys().getTickets());
    }

    @Bean
    public Binding documentsBinding(Queue documentsQueue, DirectExchange cmdDirectExchange, AndesStayRabbitProperties properties) {
        return BindingBuilder.bind(documentsQueue).to(cmdDirectExchange).with(properties.getRoutingKeys().getDocuments());
    }

    @Bean
    public Binding reservationsBinding(Queue reservationsQueue, TopicExchange topicExchange, AndesStayRabbitProperties properties) {
        return BindingBuilder.bind(reservationsQueue).to(topicExchange).with(properties.getRoutingKeys().getReservations());
    }
}
