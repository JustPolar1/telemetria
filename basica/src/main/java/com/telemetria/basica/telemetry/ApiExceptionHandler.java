package com.telemetria.basica.telemetry;

import java.util.Map;
import java.util.stream.Collectors;

import com.telemetria.basica.parcel.ParcelNotFoundException;
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

	@ExceptionHandler(ParcelNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleParcelNotFound(ParcelNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
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
		return ResponseEntity.badRequest().body(Map.of("error", "El identificador de la parcela no es válido"));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, String>> handleUnreadableRequest(HttpMessageNotReadableException exception) {
		return ResponseEntity.badRequest().body(Map.of("error",
				"El JSON no es válido o contiene un valor no permitido; revisa el tipo de suelo y los campos enviados"));
	}
}
