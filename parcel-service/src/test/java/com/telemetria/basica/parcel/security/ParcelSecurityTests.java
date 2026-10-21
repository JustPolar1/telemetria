package com.telemetria.basica.parcel.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ParcelSecurityTests {

	private static final SecretKey TEST_KEY = new SecretKeySpec(new byte[32], "HmacSHA256");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SecurityErrorHandlers errorHandlers;

	@Test
	void allowsHealthCheckWithoutToken() throws Exception {
		mockMvc.perform(get("/health"))
				.andExpect(status().isOk());
	}

	@Test
	void rejectsProtectedEndpointWithoutToken() throws Exception {
		mockMvc.perform(get("/api/v1/parcels"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("Autenticación requerida"));
	}

	@Test
	void rejectsInvalidBearerToken() throws Exception {
		mockMvc.perform(get("/api/v1/parcels")
				.header("Authorization", "Bearer malformed"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void allowsCorsPreflightFromConfiguredOrigin() throws Exception {
		mockMvc.perform(options("/api/v1/parcels")
				.header("Origin", "http://localhost:4200")
				.header("Access-Control-Request-Method", "POST")
				.header("Access-Control-Request-Headers", "authorization,content-type"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
				.andExpect(header().string("Access-Control-Allow-Methods",
						org.hamcrest.Matchers.containsString("POST")));
	}

	@Test
	void rejectsCorsPreflightFromUnconfiguredOrigin() throws Exception {
		mockMvc.perform(options("/api/v1/parcels")
				.header("Origin", "https://untrusted.example")
				.header("Access-Control-Request-Method", "POST"))
				.andExpect(status().isForbidden());
	}

	@Test
	void acceptsValidBearerToken() throws Exception {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("telemetry-service")
				.subject("test-user")
				.issuedAt(now)
				.expiresAt(now.plusSeconds(60))
				.build();
		JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(TEST_KEY));
		String token = encoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

		mockMvc.perform(get("/api/v1/parcels")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void accessDeniedResponseDoesNotExposeExceptionDetails() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin");
		MockHttpServletResponse response = new MockHttpServletResponse();

		errorHandlers.accessDeniedHandler().handle(
				request, response, new AccessDeniedException("sensitive exception details"));

		assertEquals(403, response.getStatus());
		assertTrue(response.getContentType().startsWith("application/json"));
		assertEquals("{\"error\":\"Permisos insuficientes\"}", response.getContentAsString());
		assertFalse(response.getContentAsString().contains("sensitive"));
	}
}
