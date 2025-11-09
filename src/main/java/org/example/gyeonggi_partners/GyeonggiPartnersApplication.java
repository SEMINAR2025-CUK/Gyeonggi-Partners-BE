package org.example.gyeonggi_partners;

import org.example.gyeonggi_partners.common.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class GyeonggiPartnersApplication {

    public static void main(String[] args) {
        SpringApplication.run(GyeonggiPartnersApplication.class, args);
    }

}
