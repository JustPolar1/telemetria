package com.telemetria.incidents;

import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class IncidentService {
    private final IncidentRepository repository;

    public IncidentService(IncidentRepository repository) { this.repository = repository; }

    public Incident create(IncidentRequest request) {
        return save(new Incident(null, UUID.randomUUID().toString(), request.sensorId(),
                request.titulo().strip(), request.descripcion().strip(), request.estado(), Instant.now()));
    }

    public Page<Incident> list(int page, int size) {
        return repository.findAll(PageRequest.of(page, size, Sort.by("fechaCreacion").descending()));
    }

    public Incident find(String id) {
        return repository.findById(id).orElseThrow(() -> new IncidentNotFoundException(id));
    }

    public Incident update(String id, IncidentRequest request) {
        Incident current = find(id);
        return save(new Incident(current.id(), current.folio(), request.sensorId(),
                request.titulo().strip(), request.descripcion().strip(), request.estado(), current.fechaCreacion()));
    }

    public void delete(String id) { repository.delete(find(id)); }

    private Incident save(Incident incident) {
        try { return repository.save(incident); }
        catch (DuplicateKeyException exception) { throw new DuplicateIncidentException(); }
    }
}
