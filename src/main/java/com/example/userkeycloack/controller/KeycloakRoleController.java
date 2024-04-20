package com.example.userkeycloack.controller;

import com.example.userkeycloack.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class KeycloakRoleController {
    private final RoleService roleService;

    @Operation(summary = "Assign role to user", description = "Assigns a role to a user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Role assigned successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/assign-role/{userId}")
    public ResponseEntity<Void> assignRole(@PathVariable String userId, @RequestParam String roleName){
        roleService.assignRole(userId,roleName);
        return ResponseEntity.noContent().build();
    }
}
