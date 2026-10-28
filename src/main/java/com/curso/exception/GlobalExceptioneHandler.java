package com.curso.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.access.AccessDeniedException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptioneHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptioneHandler.class);

    public record ErrorResponse(int status, String message){}

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handelNotFound(ResourceNotFoundException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, exception.getMessage()));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException exception){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, exception.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handelValidation(MethodArgumentNotValidException exception){
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": "+ error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        if (message.isBlank()){
            message = "Los datos enviados no son validos";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(HttpMessageNotReadableException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "El cuerpo de la solicitud falta o tiene un formato invalido"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400,
                        "El parámetro '" + exception.getName() + "' tiene un formato inválido"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception exception) {

        log.error("Error inesperado al procesar la solicitud", exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Ocurrió un error interno al procesar la solicitud"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException exception){
        String message = exception.getReason() != null ? exception.getReason() : "no se pudo procesar la solicitud";

        return ResponseEntity.status(exception.getStatusCode())
                .body(new ErrorResponse(exception.getStatusCode().value(), message));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN).
                body(new ErrorResponse(403, "No tienes permiso para realizar esta operación"));
    }
}
