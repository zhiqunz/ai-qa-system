package com.ai.qa.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import com.ai.qa.user.infrastructure.config.JwtProperties;

@SpringBootApplication
@EnableDiscoveryClient // (在新版中可选，但建议保留)
@EnableFeignClients
@EnableConfigurationProperties(JwtProperties.class)
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
