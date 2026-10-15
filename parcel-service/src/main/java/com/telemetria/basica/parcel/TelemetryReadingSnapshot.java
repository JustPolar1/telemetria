package com.telemetria.basica.parcel;

import java.math.BigDecimal;
import java.time.Instant;

public record TelemetryReadingSnapshot(
		Long id,
		String sensorId,
		String metric,
		BigDecimal value,
		Instant measuredAt,
		Instant recordedAt) {
}
