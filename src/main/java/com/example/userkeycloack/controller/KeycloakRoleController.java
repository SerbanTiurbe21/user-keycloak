package com.example.userkeycloack.controller;

import com.example.userkeycloack.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class KeycloakRoleController {
    private final RoleService roleService;

    @PutMapping("/assign-role/user/{userId}")
    public ResponseEntity<Void> assignRole(@PathVariable String userId, @RequestParam String roleName){
        roleService.assignRole(userId,roleName);
        return ResponseEntity.noContent().build();
    }
}
