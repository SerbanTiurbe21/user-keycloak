package com.example.userkeycloack.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;

@Data
public class User {
    @NonNull
    private String username;
    @NonNull
    private String email;
    @NonNull
    private String lastName;
    @NonNull
    private String firstName;
    @NonNull
    private String password;
    private String role;
}
