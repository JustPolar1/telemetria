package com.telemetria.basica.parcel;

import java.time.Instant;
import java.util.List;

public record ParcelDetailsResponse(
		Long id,
		String name,
		ParcelLocationResponse location,
		SoilType soilType,
		String ownerOrLead,
		String sensorId,
		Instant createdAt,
		String telemetryStatus,
		List<TelemetryReadingSnapshot> latestReadings) {

	public static ParcelDetailsResponse from(
			ParcelResponse parcel, String telemetryStatus, List<TelemetryReadingSnapshot> readings) {
		return new ParcelDetailsResponse(
				parcel.id(),
				parcel.name(),
				parcel.location(),
				parcel.soilType(),
				parcel.ownerOrLead(),
				parcel.sensorId(),
				parcel.createdAt(),
				telemetryStatus,
				readings);
	}
}
