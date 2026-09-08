package uz.insonline.travel.certificate.config;

import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import uz.insonline.travel.certificate.api.CertificateRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Очередь генерации сертификатов. Выпуск полисов только публикует заявку и сразу отвечает
 * авиакомпании (ТЗ п. 7.5: синхронный ответ не более 5 секунд), документы готовятся асинхронно.
 * Сообщения, не обработанные после ретраев, уходят в DLQ для разбора дежурным.
 */
@Configuration
@RequiredArgsConstructor
public class CertificateRabbitConfig {

    private final CertificateProperties properties;

    /**
     * Слушатель разбирает сообщение как {@link CertificateRequest} независимо от заголовка типа:
     * после выделения сервиса в отдельный микросервис класс отправителя будет лежать в другом пакете.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory certificateListenerContainerFactory(
            ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setDefaultType(CertificateRequest.class);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setClassMapper(classMapper);

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }

    @Bean
    public TopicExchange certificateExchange() {
        return new TopicExchange(properties.getRabbit().getExchange(), true, false);
    }

    @Bean
    public Queue certificateQueue() {
        return QueueBuilder.durable(properties.getRabbit().getQueue())
                .deadLetterExchange(properties.getRabbit().getExchange())
                .deadLetterRoutingKey(properties.getRabbit().getDeadLetterRoutingKey())
                .build();
    }

    @Bean
    public Queue certificateDeadLetterQueue() {
        return QueueBuilder.durable(properties.getRabbit().getDeadLetterQueue()).build();
    }

    @Bean
    public Binding certificateBinding() {
        return BindingBuilder.bind(certificateQueue()).to(certificateExchange())
                .with(properties.getRabbit().getRoutingKey());
    }

    @Bean
    public Binding certificateDeadLetterBinding() {
        return BindingBuilder.bind(certificateDeadLetterQueue()).to(certificateExchange())
                .with(properties.getRabbit().getDeadLetterRoutingKey());
    }
}
