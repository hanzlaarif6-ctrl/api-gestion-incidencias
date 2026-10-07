package com.hanzlaarif.incidencias.config;

import com.hanzlaarif.incidencias.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class AppConfig {

    /** Reloj inyectable: en los tests se sustituye por uno fijo para probar la caducidad de los tokens. */
    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
