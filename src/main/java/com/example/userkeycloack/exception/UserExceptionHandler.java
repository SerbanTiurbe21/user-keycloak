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

    @ExceptionHandler(value = {InvalidPasswordException.class})
    public ResponseEntity<Object> handleInvalidPasswordException(InvalidPasswordException invalidPasswordException) {
        UserException userException = new UserException(
                invalidPasswordException.getMessage(),
                HttpStatus.BAD_REQUEST
        );
        return new ResponseEntity<>(userException, userException.getHttpStatus());
    }

//    @ExceptionHandler(value = {UserCreationException.class})
//    public ResponseEntity<Object> handleUserCreationException(UserCreationException userCreationException) {
//        UserException userException = new UserException(
//                userCreationException.getMessage(),
//                userCreationException.getMessage().contains("status: 409") ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST
//        );
//        return new ResponseEntity<>(userException, userException.getHttpStatus());
//    }

    @ExceptionHandler(value = {InvalidRoleException.class})
    public ResponseEntity<Object> handleInvalidRoleException(InvalidRoleException invalidRoleException) {
        UserException userException = new UserException(
                invalidRoleException.getMessage(),
                HttpStatus.BAD_REQUEST
        );
        return new ResponseEntity<>(userException, userException.getHttpStatus());
    }
}
