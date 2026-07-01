package ru.moskalev.hotel_reservation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hotel reservation")
                        .version("1.0.0")
                        .description("Описание API")
                        .contact(new Contact()
                                .name("Vasiliy Moskalev")
                                .email("dev@example.com")));
    }
}