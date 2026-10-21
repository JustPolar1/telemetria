package com.telemetria.basica.security;

import jakarta.validation.Valid;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
	private final TokenService tokenService;

	public AuthController(TokenService tokenService) {
		this.tokenService = tokenService;
	}

	@PostMapping("/token")
	public ResponseEntity<?> issueToken(@Valid @RequestBody AuthRequest request) {
		try {
			return ResponseEntity.ok(tokenService.issueToken(request));
		} catch (AuthenticationException exception) {
			logger.warn("Token issuance rejected due to invalid credentials");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "Credenciales inválidas"));
		}
	}
}
