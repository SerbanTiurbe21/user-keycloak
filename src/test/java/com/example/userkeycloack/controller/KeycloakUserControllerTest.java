package com.example.userkeycloack.controller;

import com.example.userkeycloack.model.User;
import com.example.userkeycloack.model.UserDTO;
import com.example.userkeycloack.service.KeycloakUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class KeycloakUserControllerTest {
    @Mock
    private KeycloakUserService keycloakUserService;

    @InjectMocks
    private KeycloakUserController keycloakUserController;

    @Test
    void createdUserShouldReturnUser() {
        User mockUser = new User(
                "username",
                "email",
                "lastName",
                "firstName",
                "password"
        );
        when(keycloakUserService.createUser(mockUser)).thenReturn(mockUser);
        ResponseEntity<User> response = keycloakUserController.createUser(mockUser);
        verify(keycloakUserService).createUser(mockUser);
        assertEquals(mockUser, response.getBody());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getUserShouldReturnUserRepresentation() {
        String userId = "userId";
        when(keycloakUserService.getUser(userId)).thenReturn(null);
        ResponseEntity<UserDTO> response = keycloakUserController.getUser(userId);
        verify(keycloakUserService).getUser(userId);
        assertNull(response.getBody());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void deleteUserShouldReturnNoContent() {
        String userId = "userId";
        ResponseEntity<Void> response = keycloakUserController.deleteUser(userId);
        verify(keycloakUserService, times(1)).deleteUserById(userId);
        assertNull(response.getBody());
        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    void updatePasswordShouldReturnNoContent() {
        String userId = "userId";
        ResponseEntity<Void> response = keycloakUserController.updatePassword(userId);
        verify(keycloakUserService, times(1)).updatePassword(userId);
        assertNull(response.getBody());
        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    void forgotPasswordShouldReturnNoContent() {
        String username = "username";
        ResponseEntity<Void> response = keycloakUserController.forgotPassword(username);
        verify(keycloakUserService, times(1)).forgotPassword(username);
        assertNull(response.getBody());
        assertEquals(204, response.getStatusCode().value());
    }

}
