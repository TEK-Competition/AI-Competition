package com.laijiaxiang.supreme.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * http://localhost:2333/swagger-ui/index.html
 */
@Profile({"local","sit","uat"})
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI旅行规划助手-接口文档")
                        .description("利用AI生成旅行规划")
                        .version("v1"));
    }
}
