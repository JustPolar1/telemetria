package com.telemetria.incidents;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class IncidentServiceTests {
    private IncidentRepository repository;
    private IncidentService service;
    private final IncidentRequest request = new IncidentRequest("sensor-01", " Fallo ", " Sin lecturas ", IncidentStatus.ABIERTA);
    private final Incident existing = new Incident("id", "folio", "sensor-01", "Fallo", "Sin lecturas", IncidentStatus.ABIERTA, Instant.parse("2026-11-11T00:00:00Z"));

    @BeforeEach void setup() {
        repository = mock(IncidentRepository.class);
        service = new IncidentService(repository);
    }

    @Test void createsTrimmedIncidentWithGeneratedFolioAndDate() {
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        Incident result = service.create(request);
        assertThat(result.sensorId()).isEqualTo("sensor-01");
        assertThat(result.titulo()).isEqualTo("Fallo");
        assertThat(result.descripcion()).isEqualTo("Sin lecturas");
        assertThat(result.estado()).isEqualTo(IncidentStatus.ABIERTA);
        assertThat(result.folio()).isNotBlank();
        assertThat(result.fechaCreacion()).isNotNull();
    }

    @Test void listsPaginatedIncidents() {
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(existing)));
        assertThat(service.list(0, 20).getContent()).containsExactly(existing);
    }

    @Test void findsExistingIncident() {
        when(repository.findById("id")).thenReturn(Optional.of(existing));
        assertThat(service.find("id")).isEqualTo(existing);
    }

    @Test void updatesPreservingIdentityAndCreationDate() {
        when(repository.findById("id")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        Incident result = service.update("id", new IncidentRequest("sensor-02", "Reparado", "Revisión", IncidentStatus.RESUELTA));
        assertThat(result.id()).isEqualTo(existing.id());
        assertThat(result.folio()).isEqualTo(existing.folio());
        assertThat(result.fechaCreacion()).isEqualTo(existing.fechaCreacion());
        assertThat(result.estado()).isEqualTo(IncidentStatus.RESUELTA);
        assertThat(result.sensorId()).isEqualTo("sensor-02");
    }

    @Test void deletesExistingIncident() {
        when(repository.findById("id")).thenReturn(Optional.of(existing));
        service.delete("id");
        verify(repository).delete(existing);
    }

    @Test void missingIncidentFailsReadUpdateAndDelete() {
        when(repository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.find("missing")).isInstanceOf(IncidentNotFoundException.class);
        assertThatThrownBy(() -> service.update("missing", request)).isInstanceOf(IncidentNotFoundException.class);
        assertThatThrownBy(() -> service.delete("missing")).isInstanceOf(IncidentNotFoundException.class);
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
    }

    @Test void translatesDuplicateKeyIntoBusinessException() {
        when(repository.save(any())).thenThrow(new DuplicateKeyException("internal database detail"));
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(DuplicateIncidentException.class)
                .hasMessage("Ya existe una incidencia con ese folio");
    }
}
