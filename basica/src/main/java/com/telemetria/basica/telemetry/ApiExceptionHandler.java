package com.telemetria.basica.telemetry;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
	}

	@ExceptionHandler(InvalidMeasurementException.class)
	public ResponseEntity<Map<String, String>> handleInvalidMeasurement(InvalidMeasurementException exception) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleInvalidRequest(MethodArgumentNotValidException exception) {
		String errors = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.distinct()
				.collect(Collectors.joining("; "));
		return ResponseEntity.badRequest().body(Map.of("error", errors));
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<Map<String, String>> handleInvalidPathParameter(
			HandlerMethodValidationException exception) {
		return ResponseEntity.badRequest().body(Map.of("error", "Uno o más parámetros de la solicitud no son válidos"));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, String>> handleUnreadableRequest(HttpMessageNotReadableException exception) {
		return ResponseEntity.badRequest().body(Map.of("error",
				"El JSON no es válido o contiene un valor no permitido; revisa los campos y el tipo de medición"));
	}
}
