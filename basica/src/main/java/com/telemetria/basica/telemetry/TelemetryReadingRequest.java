package com.telemetria.basica.telemetry;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TelemetryReadingRequest(
		@NotBlank @Size(max = 100) String sensorId,
		@NotNull SensorMetric metric,
		@NotNull @Digits(integer = 8, fraction = 4) BigDecimal value,
		@NotNull Instant measuredAt) {
}
