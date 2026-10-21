package com.telemetria.basica.parcel;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class TelemetryTokenProvider {

	private final RestClient restClient;
	private final String username;
	private final String password;
	private volatile CachedToken cachedToken;

	public TelemetryTokenProvider(
			RestClient telemetryRestClient,
			@Value("${telemetry.service.auth.username}") String username,
			@Value("${telemetry.service.auth.password}") String password) {
		if (username.isBlank() || password.isBlank()) {
			throw new IllegalStateException("Telemetry service credentials must not be blank");
		}
		this.restClient = telemetryRestClient;
		this.username = username;
		this.password = password;
	}

	public String getAccessToken() {
		CachedToken token = cachedToken;
		if (isUsable(token)) {
			return token.value();
		}
		synchronized (this) {
			token = cachedToken;
			if (isUsable(token)) {
				return token.value();
			}
			return requestToken();
		}
	}

	private boolean isUsable(CachedToken token) {
		return token != null && token.expiresAt().isAfter(Instant.now().plusSeconds(token.refreshSkewSeconds()));
	}

	private String requestToken() {
		TelemetryAuthResponse response = restClient.post()
				.uri("/api/auth/token")
				.body(new TelemetryAuthRequest(username, password))
				.retrieve()
				.body(TelemetryAuthResponse.class);
		if (response == null
				|| response.accessToken() == null
				|| response.accessToken().isBlank()
				|| !"Bearer".equalsIgnoreCase(response.tokenType())
				|| response.expiresIn() <= 0) {
			throw new RestClientException("Telemetry Service returned an invalid access token response");
		}

		long refreshSkewSeconds = Math.min(30, Math.max(1, response.expiresIn() / 10));
		cachedToken = new CachedToken(
				response.accessToken(),
				Instant.now().plusSeconds(response.expiresIn()),
				refreshSkewSeconds);
		return response.accessToken();
	}

	private record CachedToken(String value, Instant expiresAt, long refreshSkewSeconds) {
	}
}
