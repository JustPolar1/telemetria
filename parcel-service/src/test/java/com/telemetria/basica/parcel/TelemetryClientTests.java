package com.telemetria.basica.parcel;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TelemetryClientTests {

	@Test
	void obtainsAndReusesBearerTokenForTelemetryCalls() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://telemetry.test");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		RestClient restClient = builder.build();

		server.expect(ExpectedCount.once(), requestTo("http://telemetry.test/api/auth/token"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.username").value("service-user"))
				.andExpect(jsonPath("$.password").value("service-password"))
				.andRespond(withSuccess("""
						{
						  "accessToken": "signed-token",
						  "tokenType": "Bearer",
						  "expiresIn": 900
						}
						""", MediaType.APPLICATION_JSON));
		server.expect(ExpectedCount.twice(),
				requestTo("http://telemetry.test/api/v1/sensors/sensor-01/readings/latest"))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer signed-token"))
				.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

		TelemetryTokenProvider tokenProvider = new TelemetryTokenProvider(
				restClient, "service-user", "service-password");
		TelemetryClient telemetryClient = new TelemetryClient(restClient, tokenProvider);

		assertTrue(telemetryClient.findLatestReadings("sensor-01").isEmpty());
		assertTrue(telemetryClient.findLatestReadings("sensor-01").isEmpty());
		server.verify();
	}
}
