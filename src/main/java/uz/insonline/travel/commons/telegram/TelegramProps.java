package uz.insonline.travel.commons.telegram;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "telegram")
public class TelegramProps {

    @NotBlank
    private String token;

    @NotBlank
    private String username;

    @NotNull
    private Map<String, String> chats;
}
