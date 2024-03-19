package com.example.userkeycloack.model;

import lombok.Data;

import java.util.Map;

@Data
public class UserDTO {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private boolean enabled;
    private boolean emailVerified;
    private Map<String, Boolean> access;
}
