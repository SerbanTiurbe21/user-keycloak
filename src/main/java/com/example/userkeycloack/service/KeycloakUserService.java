package com.example.userkeycloack.service;

import com.example.userkeycloack.model.User;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;

public interface KeycloakUserService {
    User createUser(User user);
    UserRepresentation getUser(String userId);
    void deleteUserById(String userId);
    UserResource getUserResource(String userId);
    void updatePassword(String userId);
    void forgotPassword(String username);
    void emailVerification(String userId);
}
