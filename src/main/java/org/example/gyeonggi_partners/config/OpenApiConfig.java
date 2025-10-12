package org.example.gyeonggi_partners.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("경기 파트너스 API")
                        .description("경기 파트너스 백엔드 API 문서")
                        .version("v1.0.0"));
    }
}
