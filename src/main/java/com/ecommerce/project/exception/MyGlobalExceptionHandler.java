package com.ecommerce.project.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class MyGlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> myMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> response = new HashMap<>();

        e.getBindingResult().getAllErrors()
                .forEach(err -> {
                    String fieldName = ((FieldError) err).getField();
                    String message = err.getDefaultMessage();
                    response.put(fieldName, message);
                });

        return new ResponseEntity<>(response, BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> myResourceNotFoundException(ResourceNotFoundException e) {
        String message = e.getMessage();
        return new ResponseEntity<>(message, NOT_FOUND);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<String> myApiException(ApiException e) {
        String message = e.getMessage();
        return new ResponseEntity<>(message, BAD_REQUEST);
    }
}