package com.example.userkeycloack.controller;

import com.example.userkeycloack.model.UpdateUserDTO;
import com.example.userkeycloack.model.User;
import com.example.userkeycloack.model.UserDTO;
import com.example.userkeycloack.service.KeycloakUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class KeycloakUserControllerTest {
    @Mock
    private KeycloakUserService keycloakUserService;

    @InjectMocks
    private KeycloakUserController keycloakUserController;

    @Test
    void createdUserShouldReturnUser() {
        final User mockUser = new User(
                "username",
                "email",
                "lastName",
                "firstName",
                "password"
        );
        when(keycloakUserService.createUser(mockUser)).thenReturn(mockUser);
        ResponseEntity<User> response = keycloakUserController.createUser(mockUser).block();
        verify(keycloakUserService).createUser(mockUser);
        assertEquals(mockUser, response.getBody());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getUserShouldReturnUserRepresentation() {
        final String userId = "userId";
        when(keycloakUserService.getUser(userId)).thenReturn(null);
        ResponseEntity<UserDTO> response = keycloakUserController.getUser(userId).block();
        verify(keycloakUserService).getUser(userId);
        assertNull(response.getBody());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void deleteUserShouldReturnNoContent() {
        final String userId = "userId";
        ResponseEntity<Void> response = keycloakUserController.deleteUser(userId).block();
        verify(keycloakUserService, times(1)).deleteUserById(userId);
        assertNull(response.getBody());
        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    void updatePasswordShouldReturnNoContent() {
        final String userId = "userId";
        ResponseEntity<Void> response = keycloakUserController.updatePassword(userId).block();
        verify(keycloakUserService, times(1)).updatePassword(userId);
        assertNull(response.getBody());
        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    void forgotPasswordShouldReturnNoContent() {
        final String username = "username";
        ResponseEntity<Void> response = keycloakUserController.forgotPassword(username).block();
        verify(keycloakUserService, times(1)).forgotPassword(username);
        assertNull(response.getBody());
        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    void getUserByEmailShouldReturnUserDTO() {
        final String email = "email@email.com";
        when(keycloakUserService.getUserByEmail(email)).thenReturn(null);
        ResponseEntity<UserDTO> response = keycloakUserController.getUserByEmail(email).block();
        verify(keycloakUserService).getUserByEmail(email);
        assertNull(response.getBody());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void shouldUpdateUser() {
        final String userId = "userId";
        final UpdateUserDTO updateUserDTO = UpdateUserDTO.builder().firstName("firstName").lastName("lastName").role("HR").username("username").email("email").build();
        doNothing().when(keycloakUserService).updateUser(userId, updateUserDTO);
        ResponseEntity<Void> response = keycloakUserController.updateUser(userId, updateUserDTO).block();
        verify(keycloakUserService, times(1)).updateUser(userId, updateUserDTO);
        assertNull(response.getBody());
        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    void shouldGetAllUsers() {
        when(keycloakUserService.getAllUsers()).thenReturn(null);
        ResponseEntity<List<UserDTO>> response = keycloakUserController.getAllUsers().block();
        verify(keycloakUserService).getAllUsers();
        assertNull(response.getBody());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void shouldGetAllUsersByRole() {
        final String role = "role";
        when(keycloakUserService.getAllUsersByRole(role)).thenReturn(null);
        ResponseEntity<List<UserDTO>> response = keycloakUserController.getAllUsersByRole(role).block();
        verify(keycloakUserService).getAllUsersByRole(role);
        assertNull(response.getBody());
        assertEquals(200, response.getStatusCode().value());
    }
}
