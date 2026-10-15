package com.telemetria.basica.parcel;

import java.time.Instant;

public record ParcelResponse(
		Long id,
		String name,
		ParcelLocationResponse location,
		SoilType soilType,
		String ownerOrLead,
		String sensorId,
		Instant createdAt) {

	public static ParcelResponse from(Parcel parcel) {
		return new ParcelResponse(
				parcel.getId(),
				parcel.getName(),
				new ParcelLocationResponse(parcel.getLatitude(), parcel.getLongitude()),
				parcel.getSoilType(),
				parcel.getOwnerOrLead(),
				parcel.getSensorId(),
				parcel.getCreatedAt());
	}
}
