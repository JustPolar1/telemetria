package com.telemetria.basica.parcel;

public record TelemetryAuthResponse(
		String accessToken,
		String tokenType,
		long expiresIn) {
}
