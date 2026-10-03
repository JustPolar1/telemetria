package com.telemetria.basica.telemetry;

import java.math.BigDecimal;
import java.time.Instant;

public record TelemetryReadingResponse(
		Long id,
		String sensorId,
		SensorMetric metric,
		BigDecimal value,
		Instant measuredAt,
		Instant receivedAt) {

	public static TelemetryReadingResponse from(TelemetryReading reading) {
		return new TelemetryReadingResponse(
				reading.getId(),
				reading.getSensorId(),
				reading.getMetric(),
				reading.getValue(),
				reading.getMeasuredAt(),
				reading.getReceivedAt());
	}
}
