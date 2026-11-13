package com.telemetria.incidents;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("incidents")
public record Incident(@Id String id, @Indexed(unique = true) String folio,
        String sensorId, String titulo, String descripcion, IncidentStatus estado,
        Instant fechaCreacion) { }
