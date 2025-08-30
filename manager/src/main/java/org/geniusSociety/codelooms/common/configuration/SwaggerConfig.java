package org.geniusSociety.codelooms.common.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * swagger configuration
 *
 * @author Cealus 2025/7/5
 */
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("code-looms")
                        .description("code-looms application")
                        .version("0.1.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
