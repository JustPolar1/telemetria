package com.telemetria.basica.parcel;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parcels")
public class ParcelController {

	private final ParcelService service;

	public ParcelController(ParcelService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<ParcelResponse> create(@Valid @RequestBody ParcelRequest request) {
		ParcelResponse parcel = service.create(request);
		return ResponseEntity.created(URI.create("/api/v1/parcels/" + parcel.id())).body(parcel);
	}

	@GetMapping
	public List<ParcelResponse> findAll() {
		return service.findAll();
	}

	@GetMapping("/{id}")
	public ParcelResponse findById(@PathVariable @Positive(message = "El ID debe ser positivo") Long id) {
		return service.findById(id);
	}
}
