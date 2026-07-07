package ru.moskalev.hotel_reservation.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic")
                .description("Введите логин и пароль пользователя");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("basicAuth");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("basicAuth", securityScheme))
                .addSecurityItem(securityRequirement)
                .info(new Info()
                        .title("Hotel reservation")
                        .version("1.0.0")
                        .description("Описание API")
                        .contact(new Contact()
                                .name("Vasiliy Moskalev")
                                .email("dev@example.com")));
    }
}