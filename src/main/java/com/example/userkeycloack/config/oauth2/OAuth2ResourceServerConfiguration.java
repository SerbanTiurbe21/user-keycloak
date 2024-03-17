package com.example.userkeycloack.config.oauth2;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@Configuration
public class OAuth2ResourceServerConfiguration {
    @Bean
    @Primary
    @ConditionalOnProperty(name = {
            "spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
            "spring.security.oauth2.resourceserver.jwt.useInsecureTrustManager"
    })
    ReactiveJwtDecoder insecureJwtDecoder(OAuth2ResourceServerProperties properties) {
        var jwtDecoder = NimbusReactiveJwtDecoder
                .withJwkSetUri(properties.getJwt().getJwkSetUri())
                .jwsAlgorithms(algorithms -> algorithms.add(SignatureAlgorithm.from("RS256")))
                .webClient(InsecureWebClient.getInstance())
                .build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefault());
        return jwtDecoder;
    }
}
