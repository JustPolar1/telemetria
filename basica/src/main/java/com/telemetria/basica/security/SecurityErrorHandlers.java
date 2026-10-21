package com.telemetria.basica.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class SecurityErrorHandlers {

	private static final Logger logger = LoggerFactory.getLogger(SecurityErrorHandlers.class);

	public AuthenticationEntryPoint authenticationEntryPoint() {
		return (request, response, exception) -> writeError(
				request, response, HttpServletResponse.SC_UNAUTHORIZED,
				"Autenticación requerida", "Authentication rejected");
	}

	public AccessDeniedHandler accessDeniedHandler() {
		return (request, response, exception) -> writeError(
				request, response, HttpServletResponse.SC_FORBIDDEN,
				"Permisos insuficientes", "Authorization denied");
	}

	private void writeError(
			HttpServletRequest request,
			HttpServletResponse response,
			int status,
			String message,
			String event) throws IOException {
		logger.warn("{}: {} {}", event, request.getMethod(), request.getRequestURI());
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write("{\"error\":\"" + message + "\"}");
	}
}
