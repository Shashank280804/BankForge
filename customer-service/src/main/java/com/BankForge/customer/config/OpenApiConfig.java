
package com.BankForge.customer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankForgeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BankForge Customer Service API")
                        .description("REST API for managing BankForge customers")
                        .version("v1"));
    }
}
