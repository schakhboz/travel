package uz.insonline.travel;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@SpringBootApplication
@OpenAPIDefinition(
        servers = {@Server(url = "/",description = "https://api-travel.insonline.uz")},
        info = @Info(title = "Travel API", version = "1.0", description = "Information about insurance of Travel")
)
public class TravelApplication {
    public static String TRANSACTION_ID;
    public static void main(String[] args) {

        SpringApplication.run(TravelApplication.class, args);
    }

}
