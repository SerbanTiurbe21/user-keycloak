package com.example.userkeycloack.service;

import com.example.userkeycloack.model.User;
import com.example.userkeycloack.model.UserDTO;
import org.keycloak.admin.client.resource.UserResource;

import java.util.List;

public interface KeycloakUserService {
    User createUser(User user);
    UserDTO getUser(String userId);
    void deleteUserById(String userId);
    UserResource getUserResource(String userId);
    void updatePassword(String userId);
    void forgotPassword(String username);
    void emailVerification(String userId);
    UserDTO getUserByEmail(String email);
    void updateUser(String id, String lastName);
    List<UserDTO> getAllUsers();
}