package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.common.ErrorApi;
import ar.edu.utn.frc.tup.piii.dtos.common.FieldErrorDto;
import ar.edu.utn.frc.tup.piii.dtos.common.ValidationErrorResponseDto;
import ar.edu.utn.frc.tup.piii.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApi> handleError(Exception ex) {
        ErrorApi errorApi = ErrorApi.builder()
                .timestamp(String.valueOf(System.currentTimeMillis()))
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorApi);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponseDto> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<FieldErrorDto> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> FieldErrorDto.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        ValidationErrorResponseDto response = ValidationErrorResponseDto.of(
                "Validation failed", fieldErrors, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorApi> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ErrorApi errorApi = ErrorApi.builder()
                .timestamp(String.valueOf(System.currentTimeMillis()))
                .status(HttpStatus.CONFLICT.value())  // 409 - Conflict
                .error("Conflict")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorApi);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorApi> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        ErrorApi errorApi = ErrorApi.builder()
                .timestamp(String.valueOf(System.currentTimeMillis()))
                .status(HttpStatus.CONFLICT.value())  // 409 - Conflict
                .error("Conflict")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorApi);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorApi> handleUserNotFound(UserNotFoundException ex) {
        ErrorApi errorApi = ErrorApi.builder()
                .timestamp(String.valueOf(System.currentTimeMillis()))
                .status(HttpStatus.NOT_FOUND.value())  // 404
                .error("Not Found")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorApi);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorApi> handleInvalidCredentials(InvalidCredentialsException ex) {
        ErrorApi errorApi = ErrorApi.builder()
                .timestamp(String.valueOf(System.currentTimeMillis()))
                .status(HttpStatus.UNAUTHORIZED.value())  // 401
                .error("Unauthorized")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorApi);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorApi> handleGeneric(RuntimeException ex) {
        ErrorApi errorApi = ErrorApi.builder()
                .timestamp(String.valueOf(System.currentTimeMillis()))
                .status(HttpStatus.BAD_REQUEST.value())  // 400 - Solo para casos genéricos
                .error("Bad Request")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorApi);
    }

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<ErrorApi> handleGameNotFound(GameNotFoundException ex) {
        ErrorApi errorApi = ErrorApi.builder()
                .timestamp(String.valueOf(System.currentTimeMillis()))
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorApi);
    }


    @ExceptionHandler(PlayerNotFoundException.class)
    public ResponseEntity<String> handlePlayerNotFound(PlayerNotFoundException ex) {
            return ResponseEntity.notFound().build();
    }


}
