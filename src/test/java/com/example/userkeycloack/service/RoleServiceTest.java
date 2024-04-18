package com.example.userkeycloack.service;

import com.example.userkeycloack.exception.InvalidRoleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
    @Mock
    private Keycloak keycloak;
    @Mock
    private KeycloakUserService keycloakUserService;
    @Mock
    private UserResource userResource;
    @Mock
    private RolesResource rolesResource;
    @Mock
    private RoleResource roleResource;
    @Mock
    private RoleRepresentation roleRepresentation;
    @Mock
    private RealmResource realmResource;
    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private RoleScopeResource roleScopeResource;
    @InjectMocks
    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(roleService, "realm", "myRealm");
    }

    @Test
    void shouldAssignRole() {
        final String userId = "userId";
        final String roleName = "DEVELOPER";
        final String realm = "myRealm";

        testSetup(userId, realm, roleName);

        roleService.assignRole(userId, roleName);

        verify(roleScopeResource, times(1)).add(Collections.singletonList(roleRepresentation));
    }

    @Test
    void shouldThrowInvalidRoleExceptionForNonDeveloperOrHRRole() {
        final String userId = "userId";
        final String invalidRoleName = "INVALID_ROLE";

        Exception exception = assertThrows(InvalidRoleException.class, () -> {
            roleService.assignRole(userId, invalidRoleName);
        });

        String expectedMessage = "Invalid role: " + invalidRoleName + ". Only DEVELOPER or HR roles are allowed.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
    private void testSetup(String userId, String realm, String roleName) {
        when(keycloakUserService.getUserResource(userId)).thenReturn(userResource);
        when(keycloak.realm(realm)).thenReturn(realmResource);
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get(roleName)).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(roleRepresentation);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
    }
}

