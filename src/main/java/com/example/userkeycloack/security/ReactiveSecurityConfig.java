package com.example.userkeycloack.security;

import com.example.userkeycloack.jwt.JwtAuthConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class ReactiveSecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf().disable() // Disable CSRF protection for a stateless API
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/eureka/**").permitAll() // Permit all requests to Eureka endpoints
                        .pathMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        .anyExchange().authenticated() // Require authentication for all other requests
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthConverter) // Use the JWT converter
                        )
                );

        return http.build();
    }
}
