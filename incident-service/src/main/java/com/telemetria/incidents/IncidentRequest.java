package com.telemetria.incidents;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record IncidentRequest(
        @NotBlank @Size(max = 100) @Pattern(regexp = "[a-zA-Z0-9_-]+") String sensorId,
        @NotBlank @Size(max = 150) String titulo,
        @NotBlank @Size(max = 2000) String descripcion,
        @NotNull IncidentStatus estado) { }
