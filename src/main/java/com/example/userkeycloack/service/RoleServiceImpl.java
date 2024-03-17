package com.example.userkeycloack.service;

import com.example.userkeycloack.exception.InvalidRoleException;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    @Value("${keycloak.realm}")
    private String realm;
    private final Keycloak keycloak;
    private final KeycloakUserService keycloakUserService;
    private static final String ROLE_DEVELOPER = "DEVELOPER";
    private static final String ROLE_HR = "HR";

    @Override
    public void assignRole(String userId, String roleName) {
        if(!roleName.equals(ROLE_DEVELOPER) && !roleName.equals(ROLE_HR)){
            throw new InvalidRoleException("Invalid role: " + roleName + ". Only DEVELOPER or HR roles are allowed.");
        }
        UserResource userResource = keycloakUserService.getUserResource(userId);
        RolesResource rolesResource = getRolesResource();
        RoleRepresentation representation = rolesResource.get(roleName).toRepresentation();
        userResource.roles().realmLevel().add(Collections.singletonList(representation));
    }

    private RolesResource getRolesResource() {
        return keycloak.realm(realm).roles();
    }

}
