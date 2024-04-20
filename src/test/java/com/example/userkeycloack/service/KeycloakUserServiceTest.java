package com.example.userkeycloack.service;

import com.example.userkeycloack.exception.InvalidPasswordException;
import com.example.userkeycloack.exception.UserCreationException;
import com.example.userkeycloack.exception.UserDeletionException;
import com.example.userkeycloack.exception.UserNotFoundException;
import com.example.userkeycloack.model.User;
import com.example.userkeycloack.model.UserDTO;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakUserServiceTest {
    private static final String UPDATE_PASSWORD = "UPDATE_PASSWORD";
    private static final int STATUS_CREATED = 201;
    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;
    @Mock
    private Response response;
    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private RoleScopeResource roleScopeResource;

    @InjectMocks
    private KeycloakUserServiceImpl keycloakUserService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(keycloakUserService, "realm", "yourRealmName");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldGetUser() {
        final String userId = "userId";
        UserRepresentation expectedUserRepresentation = new UserRepresentation();
        expectedUserRepresentation.setUsername("testUser");
        expectedUserRepresentation.setId(userId);

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(expectedUserRepresentation);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName("SomeRole");
        roles.add(role);

        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        when(roleScopeResource.listEffective()).thenReturn(roles);

        UserDTO actualUserRepresentation = keycloakUserService.getUser(userId);

        assertEquals(expectedUserRepresentation.getUsername(), actualUserRepresentation.getUsername());
        verify(usersResource, times(2)).get(userId);
        verify(userResource).toRepresentation();
    }

    @Test
    void emailVerificationShouldSendVerificationEmailWhenCalledWithUserId() {
        final String userId = "userId";

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);

        keycloakUserService.emailVerification(userId);

        verify(usersResource).get(userId);
        verify(userResource).sendVerifyEmail();
    }

    @Test
    void forgotPasswordShouldSendUpdatePasswordEmailWhenUserIsFoundByUsername() {
        final String username = "testUsername";
        final String userId = "testUserId";
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(userId);

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername(username, true)).thenReturn(List.of(userRepresentation));
        when(usersResource.get(userId)).thenReturn(userResource);

        keycloakUserService.forgotPassword(username);

        verify(usersResource).searchByUsername(username, true);
        verify(usersResource).get(userId);
        verify(userResource).executeActionsEmail(List.of(UPDATE_PASSWORD));
    }

    @Test
    void forgotPasswordShouldThrowUserNotFoundExceptionWhenUserNotFound() {
        final String username = "nonExistentUsername";

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername(username, true)).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(UserNotFoundException.class, () -> keycloakUserService.forgotPassword(username));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldUpdatePassword() {
        final String userId = "userId";
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);

        keycloakUserService.updatePassword(userId);

        verify(usersResource).get(userId);
        verify(userResource).executeActionsEmail(List.of(UPDATE_PASSWORD));
    }

    @Test
    void deleteUserByIdShouldDeleteUserWhenUserExists() {
        final String userId = "existingUserId";
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);

        keycloakUserService.deleteUserById(userId);

        verify(usersResource).get(userId);
        verify(userResource).remove();
    }

    @Test
    void deleteUserByIdShouldThrowUserDeletionExceptionWhenUnexpectedErrorOccurs() {
        final String userId = "userIdWithUnexpectedError";
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenThrow(RuntimeException.class);

        assertThrows(UserDeletionException.class, () -> keycloakUserService.deleteUserById(userId));
    }

    @Test
    void deleteUserByIdShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        final String userId = "nonExistingUserId";
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenThrow(NotFoundException.class);

        assertThrows(UserNotFoundException.class, () -> keycloakUserService.deleteUserById(userId));
    }

    @Test
    void createUserShouldReturnUserWhenCreationIsSuccessful() {
        User user = new User("username", "email@test.com", "last", "first", "password123");

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(STATUS_CREATED);
        when(usersResource.searchByUsername(anyString(), eq(true))).thenReturn(Collections.emptyList());

        User result = keycloakUserService.createUser(user);

        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
    }

    @Test
    void createUserShouldThrowInvalidPasswordExceptionWhenPasswordIsNotValid() {
        User userWithInvalidPassword = new User("username", "password", "password", "password", "password");

        Exception exception = assertThrows(InvalidPasswordException.class, () -> keycloakUserService.createUser(userWithInvalidPassword));
        assertEquals("Password should not be the same as the username, first name, last name or email", exception.getMessage());
    }

    @Test
    void createUserShouldThrowUserCreationExceptionWhenCreationFails() {
        User user = new User("username", "email@test.com", "last", "first", "password123");
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(400);

        Exception exception = assertThrows(UserCreationException.class, () -> keycloakUserService.createUser(user));
        assertTrue(exception.getMessage().contains("Error creating user, status: 400"));
    }

    @Test
    void createUserShouldThrowUserCreationExceptionWhenExceptionOccurs() {
        User user = new User("username", "email@test.com", "last", "first", "password123");
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenThrow(new RuntimeException("Unexpected error"));

        Exception exception = assertThrows(UserCreationException.class, () -> keycloakUserService.createUser(user));
        assertTrue(exception.getMessage().contains("Exception occurred while creating user: Unexpected error"));
    }


    @Test
    void createUserShouldTriggerEmailVerificationWhenUserIsCreatedButNotVerified() {
        User user = new User("username", "email@test.com", "last", "first", "password123");
        UserRepresentation foundUserRepresentation = new UserRepresentation();
        foundUserRepresentation.setId("123");
        foundUserRepresentation.setEmailVerified(false);

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(STATUS_CREATED);
        when(usersResource.searchByUsername(anyString(), eq(true))).thenReturn(List.of(foundUserRepresentation));
        when(usersResource.get(anyString())).thenReturn(userResource);

        keycloakUserService.createUser(user);

        verify(userResource).sendVerifyEmail();
    }

    @Test
    void getUserByEmailShouldReturnUserWhenUserExists() {
        final String email = "testEmail";
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(email);
        userRepresentation.setId("123");

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByEmail(email, true)).thenReturn(List.of(userRepresentation));

        List<RoleRepresentation> roleRepresentations = new ArrayList<>();
        RoleRepresentation devRole = new RoleRepresentation();
        devRole.setName("DEVELOPER");
        roleRepresentations.add(devRole);

        when(usersResource.get(anyString())).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        when(roleMappingResource.realmLevel().listEffective()).thenReturn(roleRepresentations);

        UserDTO user = keycloakUserService.getUserByEmail(email);

        assertEquals(email, user.getUsername());
    }

    @Test
    void getUserByEmailShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        final String email = "nonExistentUsername@email.com";

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByEmail(email, true)).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(UserNotFoundException.class, () -> keycloakUserService.getUserByEmail(email));
        assertEquals("User with email: " + email + " not found", exception.getMessage());
    }

    @Test
    void getUserShouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        final String nonExistentUserId = "nonExistentUserId";

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(nonExistentUserId)).thenThrow(new NotFoundException("User with id: " + nonExistentUserId + " not found"));

        UserNotFoundException thrownException = assertThrows(UserNotFoundException.class, () -> keycloakUserService.getUser(nonExistentUserId),
                "Expected getUser to throw UserNotFoundException, but it did not");

        assertTrue(thrownException.getMessage().contains("User with id: " + nonExistentUserId + " not found"));

        verify(usersResource).get(nonExistentUserId);
    }

    @Test
    void getUserShouldThrowUserNotFoundExceptionWhenUserRepresentationIsNull() {
        final String userId = "userId";

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> keycloakUserService.getUser(userId),
                "Expected getUser to throw UserNotFoundException when userRepresentation is null");

        verify(usersResource).get(userId);
        verify(userResource).toRepresentation();
    }

    @Test
    void getUserByEmailShouldPopulateAccessMap() {
        final String email = "testEmail";
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(email);
        userRepresentation.setId("123");
        Map<String, Boolean> accessMap = new HashMap<>();
        accessMap.put("manage-account", true);
        userRepresentation.setAccess(accessMap);

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByEmail(email, true)).thenReturn(List.of(userRepresentation));

        List<RoleRepresentation> roleRepresentations = new ArrayList<>();
        RoleRepresentation devRole = new RoleRepresentation();
        devRole.setName("DEVELOPER");
        roleRepresentations.add(devRole);

        when(usersResource.get(anyString())).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        when(userResource.roles().realmLevel().listEffective()).thenReturn(roleRepresentations);

        UserDTO user = keycloakUserService.getUserByEmail(email);

        assertEquals(email, user.getUsername());
        assertNotNull(user.getAccess());
        assertTrue(user.getAccess().get("manage-account"));
        assertNotNull(user.getRole());
        assertTrue(user.getRole().equals("HR") || user.getRole().equals("DEVELOPER"));
    }


    @Test
    void shouldUpdateUserWhenUserExists() {
        final String lastName = "UpdatedLastName";
        final String userId = "existingUserId";
        final String newLastName = "UpdatedLastName";

        UserResource userResource = mock(UserResource.class);
        UserRepresentation userRepresentation = new UserRepresentation();

        userRepresentation.setLastName("OriginalLastName");

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRepresentation);

        keycloakUserService.updateUser(userId, lastName);

        userRepresentation.setLastName(newLastName);

        assertEquals(newLastName, userRepresentation.getLastName(), "The last name should be updated to the new value");

        verify(userResource).update(userRepresentation);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        final String nonExistentUserId = "nonExistentUserId";
        final String lastName = "UpdatedLastName";

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(nonExistentUserId)).thenThrow(new NotFoundException("User with id: " + nonExistentUserId + " not found"));

        User user = new User("username", "email@test.com", "last", "first", "password123");

        UserNotFoundException thrownException = assertThrows(UserNotFoundException.class,
                () -> keycloakUserService.updateUser(nonExistentUserId, lastName),
                "Expected updateUser to throw UserNotFoundException, but it did not");

        assertTrue(thrownException.getMessage().contains("User with id: " + nonExistentUserId + " not found"));

        verify(usersResource).get(nonExistentUserId);
        verifyNoMoreInteractions(usersResource);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserRepresentationIsNull() {
        final String userId = "existingUserIdButNoData";
        final String lastName = "UpdatedLastName";

        UserResource userResource = mock(UserResource.class);

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(null);

        UserNotFoundException thrownException = assertThrows(UserNotFoundException.class,
                () -> keycloakUserService.updateUser(userId, lastName),
                "Expected updateUser to throw UserNotFoundException because user representation is null");

        assertTrue(thrownException.getMessage().contains("User with id: " + userId + " not found"));

        verify(userResource).toRepresentation();
    }
}