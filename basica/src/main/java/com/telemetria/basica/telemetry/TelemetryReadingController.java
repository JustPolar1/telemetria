package com.telemetria.basica.telemetry;

import java.net.URI;
import java.time.Instant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
public class TelemetryReadingController {

	private final TelemetryReadingService service;

	public TelemetryReadingController(TelemetryReadingService service) {
		this.service = service;
	}

	@PostMapping("/lecturas")
	public ResponseEntity<TelemetryReadingResponse> receive(@Valid @RequestBody TelemetryReadingRequest request) {
		TelemetryReadingResponse response = service.receive(request);
		return ResponseEntity.created(URI.create("/api/lecturas/" + response.id())).body(response);
	}

	@GetMapping("/sensores/{sensorId}/lecturas")
	public Page<TelemetryReadingResponse> findBySensor(
			@PathVariable @NotBlank @Size(max = 100) String sensorId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return service.findBySensor(sensorId, from, to, page, size);
	}
}
