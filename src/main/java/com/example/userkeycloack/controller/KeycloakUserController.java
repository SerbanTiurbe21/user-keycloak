package com.example.userkeycloack.controller;

import com.example.userkeycloack.model.User;
import com.example.userkeycloack.service.KeycloakUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class KeycloakUserController {
    private final KeycloakUserService keycloakUserService;

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user){
        return ResponseEntity.ok(keycloakUserService.createUser(user));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserRepresentation> getUser(@PathVariable String userId){
        return ResponseEntity.ok(keycloakUserService.getUser(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId){
        keycloakUserService.deleteUserById(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable String userId){
        keycloakUserService.updatePassword(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{username}/forgot-password")
    public ResponseEntity<Void> forgotPassword(@PathVariable String username){
        keycloakUserService.forgotPassword(username);
        return ResponseEntity.noContent().build();
    }
}
