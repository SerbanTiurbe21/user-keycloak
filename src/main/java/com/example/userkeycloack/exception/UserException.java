package com.example.userkeycloack.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class UserException {
    private final String message;
    private final HttpStatus httpStatus;
}
