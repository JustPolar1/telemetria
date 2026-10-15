package com.telemetria.basica.telemetry;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TelemetryReadingRequest(
		@NotBlank(message = "El identificador del sensor es obligatorio")
		@Size(max = 100, message = "El identificador no puede exceder 100 caracteres")
		@Pattern(regexp = "^[A-Za-z0-9._:-]+$", message = "El identificador contiene caracteres no permitidos")
		String sensorId,
		@NotNull SensorMetric metric,
		@NotNull @Digits(integer = 8, fraction = 4) BigDecimal value,
		@NotNull Instant measuredAt) {
}
