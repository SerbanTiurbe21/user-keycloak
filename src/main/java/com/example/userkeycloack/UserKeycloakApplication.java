package com.example.userkeycloack;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@OpenAPIDefinition(info = @Info(title = "User Keycloak API", version = "1.0", description = "Documentation User Keycloak API v1.0"))
public class UserKeycloakApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserKeycloakApplication.class, args);
	}

}
