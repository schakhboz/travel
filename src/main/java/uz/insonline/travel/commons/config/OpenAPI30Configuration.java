package uz.insonline.travel.commons.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPI30Configuration {
    private static final String SECURITY_SCHEME_NAME_BASIC = "basicAuth";
    private static final String SECURITY_SCHEME_NAME_BEARER = "bearerAuth";

    @Bean
    public OpenAPI customizeOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME_BASIC, createBasicSecurityScheme())
                        .addSecuritySchemes(SECURITY_SCHEME_NAME_BEARER, createBearerSecurityScheme()))
                .addSecurityItem(createBearerSecurityRequirement())
                .addSecurityItem(createBasicSecurityRequirement());
    }

    private SecurityScheme createBearerSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME_BEARER)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER);
    }

    private SecurityScheme createBasicSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME_BASIC)
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic")
                .in(SecurityScheme.In.HEADER);
    }

    private SecurityRequirement createBearerSecurityRequirement() {
        SecurityRequirement requirement = new SecurityRequirement();
        requirement.addList(SECURITY_SCHEME_NAME_BEARER);
        return requirement;
    }

    private SecurityRequirement createBasicSecurityRequirement() {
        SecurityRequirement requirement = new SecurityRequirement();
        requirement.addList(SECURITY_SCHEME_NAME_BASIC);
        return requirement;
    }
}
