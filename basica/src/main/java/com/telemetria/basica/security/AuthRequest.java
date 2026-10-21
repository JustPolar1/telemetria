package com.telemetria.basica.security;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
		@NotBlank String username,
		@NotBlank String password) {
}
