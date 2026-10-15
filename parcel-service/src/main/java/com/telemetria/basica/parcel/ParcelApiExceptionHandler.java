package com.telemetria.basica.parcel;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class ParcelApiExceptionHandler {

	@ExceptionHandler(ParcelNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(ParcelNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
	}

	@ExceptionHandler(DuplicateSensorAssignmentException.class)
	public ResponseEntity<Map<String, String>> handleDuplicateSensor(
			DuplicateSensorAssignmentException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage()));
	}

	@ExceptionHandler(TelemetryServiceUnavailableException.class)
	public ResponseEntity<Map<String, String>> handleTelemetryUnavailable(
			TelemetryServiceUnavailableException exception) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(Map.of("error", exception.getMessage()));
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
	public ResponseEntity<Map<String, String>> handleInvalidParameter(
			HandlerMethodValidationException exception) {
		return ResponseEntity.badRequest().body(Map.of("error", "El ID de la parcela debe ser un entero positivo"));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, String>> handleInvalidId(MethodArgumentTypeMismatchException exception) {
		return ResponseEntity.badRequest().body(Map.of("error", "El ID de la parcela debe ser un entero positivo"));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, String>> handleUnreadableRequest(
			HttpMessageNotReadableException exception) {
		return ResponseEntity.badRequest().body(Map.of("error",
				"El JSON no es válido o contiene un valor no permitido; revisa soilType y los campos enviados"));
	}
}
