package com.telemetria.incidents;

import java.net.URI;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {
    private final IncidentService service;
    public IncidentController(IncidentService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Incident> create(@Valid @RequestBody IncidentRequest request) {
        Incident incident = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/incidents/" + incident.id())).body(incident);
    }

    @GetMapping
    public IncidentPage list(@RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return IncidentPage.from(service.list(page, size));
    }

    @GetMapping("/{id}")
    public Incident find(@PathVariable String id) { return service.find(id); }

    @PutMapping("/{id}")
    public Incident update(@PathVariable String id, @Valid @RequestBody IncidentRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
