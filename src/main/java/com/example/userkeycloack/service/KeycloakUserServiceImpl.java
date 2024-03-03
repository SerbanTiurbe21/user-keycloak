package com.example.userkeycloack.service;

import com.example.userkeycloack.model.User;
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

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KeycloakUserServiceImpl implements KeycloakUserService{
    private final String UPDATE_PASSWORD = "UPDATE_PASSWORD";
    private final Keycloak keycloak;
    @Value("${keycloak.realm}")
    private String realm;

    @Override
    public User createUser(User user) {
        UserRepresentation userRepresentation = getUserRepresentation(user);

        UsersResource usersResource = getUsersResource();

        Response response = usersResource.create(userRepresentation);

        if(response.getStatus() == 201){
            List<UserRepresentation> representationList = usersResource.searchByUsername(user.getUsername(), true);
            if(!representationList.isEmpty()){
                UserRepresentation userRepresentation1 = representationList.stream().filter(userRepresentation2 -> Objects.equals(false, userRepresentation.isEmailVerified())).findFirst().orElse(null);
                if(userRepresentation1 != null){
                    emailVerification(userRepresentation1.getId());
                }

            }
            return user;
        }

        return null;
    }

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
    public UserRepresentation getUser(String userId) {
        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId).toRepresentation();
    }

    @Override
    public void deleteUserById(String userId) {
        UsersResource usersResource = getUsersResource();
        usersResource.delete(userId);
    }

    @Override
    public UserResource getUserResource(String userId) {
        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId);
    }

    @Override
    public void updatePassword(String userId) {
        UserResource userResource = getUserResource(userId);
        userResource.executeActionsEmail(List.of("UPDATE_PASSWORD"));
    }

    @Override
    public void forgotPassword(String username) {
        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> userRepresentations = usersResource.searchByUsername(username, true);

        UserRepresentation userRepresentation = userRepresentations.stream().findFirst().orElseThrow(() -> new RuntimeException("User not found"));

        if(userRepresentation != null){
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
