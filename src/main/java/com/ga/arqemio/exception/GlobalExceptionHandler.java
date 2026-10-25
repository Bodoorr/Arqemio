package com.ga.arqemio.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException exception){
        return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    //returns a response containing field names and error messages
    public ResponseEntity<Map<String,String>> handleValidationException(MethodArgumentNotValidException exception){
        //creates an empty collection for validation errors
        Map<String,String> errors= new HashMap<>();
        //get validation result and errors that associated with DTO fields
        exception.getBindingResult().getFieldErrors().forEach(
                //get the field name and the custom annotation message and add it to the map
                error-> errors.put(error.getField(), error.getDefaultMessage())
        );
        //returns HTTP 400
        return ResponseEntity.badRequest().body(errors);
    }

}
