package uz.insonline.travel.commons.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfig {


    private final String host;


    private final int port;


    private final String username;


    private final String password;


    private final String smtpAuth;


    private final String starttlsEnable;

    @Autowired
    public EmailConfig(@Value("${spring.mail.host}") String host,
                       @Value("${spring.mail.port}") int port,
                       @Value("${spring.mail.username}") String username,
                       @Value("${spring.mail.password}") String password,
                       @Value("${spring.mail.properties.mail.smtp.auth}") String smtpAuth,
                       @Value("${spring.mail.properties.mail.smtp.starttls.enable}") String starttlsEnable) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.smtpAuth = smtpAuth;
        this.starttlsEnable = starttlsEnable;
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        java.util.Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);

        return mailSender;
    }
}
