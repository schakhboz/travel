package uz.insonline.travel.commons.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String SMS_QUEUE = "travel.sms.queue";
    public static final String SMS_EXCHANGE = "travel.sms.exchange";
    public static final String SMS_ROUTING_KEY = "travel.sms.send";

    @Bean
    public Queue smsQueue() {
        return new Queue(SMS_QUEUE, true);
    }

    @Bean
    public TopicExchange smsExchange() {
        return new TopicExchange(SMS_EXCHANGE);
    }

    @Bean
    public Binding smsBinding(Queue smsQueue, TopicExchange smsExchange) {
        return BindingBuilder
                .bind(smsQueue)
                .to(smsExchange)
                .with(SMS_ROUTING_KEY);
    }

    /**
     * Общий конвертер очередей. Использует ObjectMapper приложения — иначе даты java.time
     * (LocalDate в заявке на сертификат) не сериализуются.
     */
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
