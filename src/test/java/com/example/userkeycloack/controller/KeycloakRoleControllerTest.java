package com.example.userkeycloack.controller;

import com.example.userkeycloack.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class KeycloakRoleControllerTest {
    @Mock
    private RoleService roleService;
    @InjectMocks
    private KeycloakRoleController keycloakRoleController;

    @Test
    void assignRoleShouldReturnNoContent() {
        final String userId = "userId";
        final String roleName = "roleName";

        doNothing().when(roleService).assignRole(userId, roleName);

        ResponseEntity<Void> response = keycloakRoleController.assignRole(userId, roleName);

        ResponseEntity<Void> expected = ResponseEntity.noContent().build();
        assertEquals(expected.getStatusCode(), response.getStatusCode());
    }
}
