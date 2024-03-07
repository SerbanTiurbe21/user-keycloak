package com.example.userkeycloack.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class UserExceptionHandler {
    @ExceptionHandler(value = {UserNotFoundException.class})
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException userNotFoundException) {
        UserException userException = new UserException(
                userNotFoundException.getMessage(),
                HttpStatus.NOT_FOUND
        );
        return new ResponseEntity<>(userException, userException.getHttpStatus());
    }

    @ExceptionHandler(value = {UserDeletionException.class})
    public ResponseEntity<Object> handleUserDeletionException(UserDeletionException userDeletionException) {
        UserException userException = new UserException(
                userDeletionException.getMessage(),
                HttpStatus.BAD_REQUEST
        );
        return new ResponseEntity<>(userException, userException.getHttpStatus());
    }

}
