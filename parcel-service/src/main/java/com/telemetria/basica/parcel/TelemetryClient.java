package com.telemetria.basica.parcel;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class TelemetryClient {

	private final RestClient restClient;
	private final TelemetryTokenProvider tokenProvider;

	public TelemetryClient(RestClient telemetryRestClient, TelemetryTokenProvider tokenProvider) {
		this.restClient = telemetryRestClient;
		this.tokenProvider = tokenProvider;
	}

	public List<TelemetryReadingSnapshot> findLatestReadings(String sensorId) {
		try {
			List<TelemetryReadingSnapshot> readings = restClient.get()
					.uri("/api/v1/sensors/{sensorId}/readings/latest", sensorId)
					.headers(headers -> headers.setBearerAuth(tokenProvider.getAccessToken()))
					.retrieve()
					.body(new ParameterizedTypeReference<>() {
					});
			if (readings == null) {
				throw new RestClientException("Telemetry Service returned an empty response body");
			}
			return readings;
		} catch (RestClientException exception) {
			throw new TelemetryServiceUnavailableException(exception);
		}
	}
}
