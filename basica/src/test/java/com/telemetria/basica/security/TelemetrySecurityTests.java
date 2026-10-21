package com.telemetria.basica.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TelemetrySecurityTests {

	private static final String TEST_PASSWORD = "test-password";
	private static final String TEST_PASSWORD_HASH = new BCryptPasswordEncoder().encode(TEST_PASSWORD);

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SecurityErrorHandlers errorHandlers;

	@Autowired
	private ObjectMapper objectMapper;

	@DynamicPropertySource
	static void configureCredentials(DynamicPropertyRegistry registry) {
		registry.add("security.auth.password-hash", () -> TEST_PASSWORD_HASH);
	}

	@Test
	void allowsHealthCheckWithoutToken() throws Exception {
		mockMvc.perform(get("/health"))
				.andExpect(status().isOk());
	}

	@Test
	void rejectsProtectedEndpointWithoutToken() throws Exception {
		mockMvc.perform(post("/api/lecturas")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("Autenticación requerida"));
	}

	@Test
	void rejectsInvalidBearerToken() throws Exception {
		mockMvc.perform(get("/api/v1/sensors/sensor-01/readings/latest")
				.header("Authorization", "Bearer not-a-jwt"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void accessDeniedResponseDoesNotExposeExceptionDetails() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin");
		MockHttpServletResponse response = new MockHttpServletResponse();

		errorHandlers.accessDeniedHandler().handle(
				request, response, new AccessDeniedException("sensitive exception details"));

		assertEquals(403, response.getStatus());
		assertEquals("{\"error\":\"Permisos insuficientes\"}", response.getContentAsString());
		assertFalse(response.getContentAsString().contains("sensitive"));
	}

	@Test
	void allowsCorsPreflightFromConfiguredOrigin() throws Exception {
		mockMvc.perform(options("/api/lecturas")
				.header("Origin", "http://localhost:4200")
				.header("Access-Control-Request-Method", "POST")
				.header("Access-Control-Request-Headers", "authorization,content-type"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
				.andExpect(header().string("Access-Control-Allow-Methods",
						org.hamcrest.Matchers.containsString("POST")))
				.andExpect(header().string("Access-Control-Allow-Headers",
						org.hamcrest.Matchers.containsString("authorization")));
	}

	@Test
	void rejectsCorsPreflightFromUnconfiguredOrigin() throws Exception {
		mockMvc.perform(options("/api/lecturas")
				.header("Origin", "https://untrusted.example")
				.header("Access-Control-Request-Method", "POST"))
				.andExpect(status().isForbidden());
	}

	@Test
	void issuesTokenAndAllowsAuthenticatedRequest() throws Exception {
		MvcResult tokenResult = mockMvc.perform(post("/api/auth/token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(new AuthRequest("test-user", TEST_PASSWORD))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(900))
				.andReturn();

		String token = objectMapper.readTree(tokenResult.getResponse().getContentAsByteArray())
				.get("accessToken").asText();

		mockMvc.perform(post("/api/lecturas")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "sensorId": "security-test-sensor",
						  "metric": "TEMPERATURE",
						  "value": 21.5,
						  "measuredAt": "2026-10-02T18:30:00Z"
						}
						"""))
				.andExpect(status().isCreated());
	}

	@Test
	void rejectsInvalidCredentials() throws Exception {
		mockMvc.perform(post("/api/auth/token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(new AuthRequest("test-user", "wrong-password"))))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("Credenciales inválidas"));
	}
}
