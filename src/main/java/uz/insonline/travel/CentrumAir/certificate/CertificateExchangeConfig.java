package uz.insonline.travel.CentrumAir.certificate;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Объявляется только обменник: очередь, привязку и DLQ создаёт сервис сертификатов.
 * Так выпуск полисов может публиковать заявки даже до первого запуска сервиса документов.
 */
@Configuration
@RequiredArgsConstructor
public class CertificateExchangeConfig {

    private final CertificateQueueProperties properties;

    @Bean
    public TopicExchange certificateExchange() {
        return new TopicExchange(properties.getExchange(), true, false);
    }
}
