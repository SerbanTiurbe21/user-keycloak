package com.example.userkeycloack.service;

import com.example.userkeycloack.exception.*;
import com.example.userkeycloack.model.UpdateUserDTO;
import com.example.userkeycloack.model.User;
import com.example.userkeycloack.model.UserDTO;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class KeycloakUserServiceImpl implements KeycloakUserService {
    private static final String UPDATE_PASSWORD = "UPDATE_PASSWORD";
    private static final String ROLE_DEVELOPER = "DEVELOPER";
    private static final String ROLE_HR = "HR";
    private static final String ROLE_ADMIN = "admin";
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
        UserRepresentation userRepresentation;
        try {
            userRepresentation = usersResource.get(userId).toRepresentation();
        } catch (NotFoundException e) {
            throw new UserNotFoundException("User with id: " + userId + " not found");
        }

        if (userRepresentation == null) {
            throw new UserNotFoundException("User with id: " + userId + " not found");
        }

        return getUserDTO(userRepresentation);
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

    @Override
    public UserDTO getUserByEmail(String email) {
        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> userRepresentations = usersResource.searchByEmail(email, true);
        if (userRepresentations.isEmpty()) {
            throw new UserNotFoundException("User with email: " + email + " not found");
        }

        UserRepresentation userRepresentation = userRepresentations.get(0);
        return getUserDTO(userRepresentation);
    }

    @Override
    public void updateUser(String id, UpdateUserDTO updateUserDTO) {
        UserResource userResource;
        try {
            userResource = getUserResource(id);
        } catch (NotFoundException e) {
            throw new UserNotFoundException("User with id: " + id + " not found");
        }

        UserRepresentation userRepresentation = userResource.toRepresentation();
        if (userRepresentation == null) {
            throw new UserNotFoundException("User with id: " + id + " not found");
        }

        if(!Objects.equals(updateUserDTO.getUsername(), updateUserDTO.getEmail())){
            throw new InvalidUpdateException("Username and email should be the same");
        }

        if (emailExists(updateUserDTO.getEmail(), id) || emailExists(updateUserDTO.getUsername(), id)) {
            throw new InvalidUpdateException("Email already in use by another account");
        }

        userRepresentation.setUsername(updateUserDTO.getUsername());
        userRepresentation.setEmail(updateUserDTO.getEmail());
        userRepresentation.setFirstName(updateUserDTO.getFirstName());
        userRepresentation.setLastName(updateUserDTO.getLastName());

        List<RoleRepresentation> roles = userResource.roles().realmLevel().listEffective();
        roles.forEach(role -> userResource.roles().realmLevel().remove(List.of(role)));

        if(!updateUserDTO.getRole().equals(ROLE_DEVELOPER) && !updateUserDTO.getRole().equals(ROLE_HR) && !updateUserDTO.getRole().equals(ROLE_ADMIN)){
            throw new InvalidRoleException("Invalid role: " + updateUserDTO.getRole() + ". Only DEVELOPER, HR and admin roles are allowed.");
        }

        RolesResource rolesResource = getRolesResource();
        RoleRepresentation representation = rolesResource.get(updateUserDTO.getRole()).toRepresentation();
        userResource.roles().realmLevel().add(Collections.singletonList(representation));

        userResource.update(userRepresentation);
    }

    private boolean emailExists(String email, String userIdExcluded) {
        List<UserDTO> users = getAllUsers();
        return users != null && users.stream()
                .anyMatch(user -> user.getEmail() != null && user.getEmail().equalsIgnoreCase(email) && !user.getId().equals(userIdExcluded));
    }

    private RolesResource getRolesResource() {
        return keycloak.realm(realm).roles();
    }

    @Override
    public List<UserDTO> getAllUsers() {
        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> userRepresentations = usersResource.list();
        return userRepresentations.stream().map(this::getUserDTO).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getAllUsersByRole(String role) {
        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> userRepresentations = usersResource.list();
        List<UserDTO> userDTOS = new ArrayList<>();
        for (UserRepresentation userRepresentation : userRepresentations) {
            UserDTO userDTO = getUserDTO(userRepresentation);
            if (userDTO.getRole().equals(role)) {
                userDTOS.add(userDTO);
            }
        }
        return userDTOS;
    }

    private UserDTO getUserDTO(UserRepresentation userRepresentation) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userRepresentation.getId());
        userDTO.setUsername(userRepresentation.getUsername());
        userDTO.setEmail(userRepresentation.getEmail());
        userDTO.setFirstName(userRepresentation.getFirstName());
        userDTO.setLastName(userRepresentation.getLastName());
        userDTO.setEnabled(Boolean.TRUE.equals(userRepresentation.isEnabled()));
        userDTO.setEmailVerified((Boolean.TRUE.equals(userRepresentation.isEmailVerified())));

        UserResource userResource = getUsersResource().get(userRepresentation.getId());
        List<RoleRepresentation> allRoles = userResource.roles().realmLevel().listEffective();
        List<String> relevantRoles = allRoles.stream()
                .map(RoleRepresentation::getName)
                .filter(roleName -> roleName.equals("HR") || roleName.equals("DEVELOPER") || roleName.equals("admin"))
                .toList();
        if (!relevantRoles.isEmpty()) {
            userDTO.setRole(relevantRoles.get(0));
        }

        if (userRepresentation.getAccess() != null) {
            Map<String, Boolean> accessMap = new HashMap<>(userRepresentation.getAccess());
            userDTO.setAccess(accessMap);
        }
        return userDTO;
    }
}
