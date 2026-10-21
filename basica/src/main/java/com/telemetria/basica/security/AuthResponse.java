package com.telemetria.basica.security;

public record AuthResponse(
		String accessToken,
		String tokenType,
		long expiresIn) {
}
