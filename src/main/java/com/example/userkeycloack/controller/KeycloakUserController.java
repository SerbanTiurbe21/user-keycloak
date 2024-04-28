package com.example.userkeycloack.controller;

import com.example.userkeycloack.model.User;
import com.example.userkeycloack.model.UserDTO;
import com.example.userkeycloack.service.KeycloakUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class KeycloakUserController {
    private final KeycloakUserService keycloakUserService;

    @Operation(summary = "Create a new user", description = "Creates a new user with the provided details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping("/create-user")
    public ResponseEntity<User> createUser(
            @Parameter(description = "User object containing the necessary information to create a new user record. This includes personal details, qualifications, and any other relevant information.")
            @Valid @RequestBody User user){
        return ResponseEntity.ok(keycloakUserService.createUser(user));
    }

    @Operation(summary = "Get user", description = "Retrieves a user by their ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User retrieved successfully", content = @Content(schema = @Schema(implementation = UserRepresentation.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ROLE_client-hr')")
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUser(
            @Parameter(description = "ID of the user to be retrieved")
            @PathVariable String userId){
        return ResponseEntity.ok(keycloakUserService.getUser(userId));
    }

    @Operation(summary = "Delete user", description = "Deletes a user by their ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ROLE_client-hr')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to be deleted")
            @PathVariable String userId){
        keycloakUserService.deleteUserById(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update user's password", description = "Updates a user by their ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User password updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ROLE_client-hr') or hasRole('ROLE_client-developer')")
    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(
            @Parameter(description = "ID of the user to be updated")
            @PathVariable String userId){
        keycloakUserService.updatePassword(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Forgot user's password", description = "Sends a forgot password email to the user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Forgot password email sent successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ROLE_client-hr') or hasRole('ROLE_client-developer')")
    @PutMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @Parameter(description = "Username of the user to send the forgot password email")
            @RequestParam String username){
        keycloakUserService.forgotPassword(username);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get user by email", description = "Retrieves a user by their email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User retrieved successfully", content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ROLE_client-hr') or hasRole('ROLE_client-developer')")
    @GetMapping("/email")
    public ResponseEntity<UserDTO> getUserByEmail(
            @Parameter(description = "Email of the user to be retrieved")
            @RequestParam String email){
        return ResponseEntity.ok(keycloakUserService.getUserByEmail(email));
    }

    @Operation(summary = "Update user", description = "Updates a user by their ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ROLE_client-hr')")
    @PutMapping("/{userId}")
    public ResponseEntity<Void> updateUser(
            @Parameter(description = "ID of the user to be updated")
            @PathVariable String userId,
            @Parameter(description = "Last name of the user.")
            @RequestParam String lastName){
        keycloakUserService.updateUser(userId, lastName);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all users", description = "Retrieves all users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "No users found")
    })
    @PreAuthorize("hasRole('ROLE_client-hr')")
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers(){
        return ResponseEntity.ok(keycloakUserService.getAllUsers());
    }
}
