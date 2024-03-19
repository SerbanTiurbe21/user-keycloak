package com.example.userkeycloack.service;

import com.example.userkeycloack.exception.InvalidPasswordException;
import com.example.userkeycloack.exception.UserCreationException;
import com.example.userkeycloack.exception.UserDeletionException;
import com.example.userkeycloack.exception.UserNotFoundException;
import com.example.userkeycloack.model.User;
import com.example.userkeycloack.model.UserDTO;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KeycloakUserServiceImpl implements KeycloakUserService {
    private static final String UPDATE_PASSWORD = "UPDATE_PASSWORD";
    private static final int STATUS_CREATED = 201;
    private final Keycloak keycloak;
    @Value("${keycloak.realm}")
    private String realm;

    private static UserRepresentation getUserRepresentation(User user) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setUsername(user.getUsername());
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setEmailVerified(false);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setValue(user.getPassword());
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

        userRepresentation.setCredentials(List.of(credentialRepresentation));
        return userRepresentation;
    }

    private UsersResource getUsersResource() {
        RealmResource realmResource = keycloak.realm(realm);
        return realmResource.users();
    }

    @Override
    public UserDTO getUser(String userId) {
        UsersResource usersResource = getUsersResource();
        UserRepresentation userRepresentation = usersResource.get(userId).toRepresentation();
        if (userRepresentation == null) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(userRepresentation.getId());
        userDTO.setUsername(userRepresentation.getUsername());
        userDTO.setEmail(userRepresentation.getEmail());
        userDTO.setFirstName(userRepresentation.getFirstName());
        userDTO.setLastName(userRepresentation.getLastName());
        userDTO.setEnabled(Boolean.TRUE.equals(userRepresentation.isEnabled()));
        userDTO.setEmailVerified((Boolean.TRUE.equals(userRepresentation.isEmailVerified())));

        if (userRepresentation.getAccess() != null) {
            Map<String, Boolean> accessMap = new HashMap<>(userRepresentation.getAccess());
            userDTO.setAccess(accessMap);
        }
        return userDTO;
    }

    @Override
    public User createUser(User user) {
        if (user.getPassword().equals(user.getEmail()) || user.getPassword().equals(user.getUsername()) || user.getPassword().equals(user.getFirstName()) || user.getPassword().equals(user.getLastName())) {
            throw new InvalidPasswordException("Password should not be the same as the username, first name, last name or email");
        }

        UserRepresentation userRepresentation = getUserRepresentation(user);
        UsersResource usersResource = getUsersResource();

        try (Response response = usersResource.create(userRepresentation)) {
            if (response.getStatus() == STATUS_CREATED) {
                List<UserRepresentation> representationList = usersResource.searchByUsername(user.getUsername(), true);
                if (!representationList.isEmpty()) {
                    representationList.stream().filter(userRepresentation2 -> Objects.equals(false, userRepresentation.isEmailVerified())).findFirst().ifPresent(userRepresentation1 -> emailVerification(userRepresentation1.getId()));
                }
                return user;
            } else {
                throw new UserCreationException("Error creating user, status: " + response.getStatus());
            }
        } catch (Exception e) {
            throw new UserCreationException("Exception occurred while creating user: " + e.getMessage());
        }
    }

    @Override
    public void deleteUserById(String userId) {
        UsersResource usersResource = getUsersResource();
        try {
            usersResource.get(userId).remove();
        } catch (NotFoundException e) {
            throw new UserNotFoundException("User: " + userId + "not found");
        } catch (Exception e) {
            throw new UserDeletionException("Error deleting user");
        }
    }

    @Override
    public UserResource getUserResource(String userId) {
        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId);
    }

    @Override
    public void updatePassword(String userId) {
        UserResource userResource = getUserResource(userId);
        userResource.executeActionsEmail(List.of(UPDATE_PASSWORD));
    }

    @Override
    public void forgotPassword(String username) {
        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> userRepresentations = usersResource.searchByUsername(username, true);

        UserRepresentation userRepresentation = userRepresentations.stream().findFirst().orElseThrow(() -> new UserNotFoundException("User not found"));

        if (userRepresentation != null) {
            UserResource userResource = getUserResource(userRepresentation.getId());
            userResource.executeActionsEmail(List.of(UPDATE_PASSWORD));
        }
    }

    @Override
    public void emailVerification(String userId) {
        UsersResource usersResource = getUsersResource();
        usersResource.get(userId).sendVerifyEmail();
    }
}
