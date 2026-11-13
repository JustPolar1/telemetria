package com.telemetria.incidents;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class IncidentExceptionHandler {
    private ResponseEntity<Map<String, String>> error(int status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    @ExceptionHandler(IncidentNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(IncidentNotFoundException exception) {
        return error(404, exception.getMessage());
    }

    @ExceptionHandler(DuplicateIncidentException.class)
    public ResponseEntity<Map<String, String>> duplicate(DuplicateIncidentException exception) {
        return error(409, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> invalid(MethodArgumentNotValidException exception) {
        return error(400, exception.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .distinct().collect(Collectors.joining("; ")));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HandlerMethodValidationException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> malformed(Exception exception) {
        return error(400, "Solicitud inválida: revisa JSON, estado y paginación (page >= 0, size entre 1 y 100)");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> unavailable(DataAccessException exception) {
        return error(503, "No se pudo acceder a la base de datos; intenta más tarde");
    }
}
