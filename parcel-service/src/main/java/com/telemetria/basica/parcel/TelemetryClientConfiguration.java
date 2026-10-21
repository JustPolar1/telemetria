package com.telemetria.basica.parcel;

import java.time.Duration;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class TelemetryClientConfiguration {

	@Bean
	RestClient telemetryRestClient(
			@Value("${telemetry.service.url:http://localhost:8082}") String baseUrl,
			@Value("${telemetry.service.connect-timeout:2s}") Duration connectTimeout,
			@Value("${telemetry.service.read-timeout:3s}") Duration readTimeout,
			@Value("${telemetry.service.tls.trust-store:}") String trustStoreLocation,
			@Value("${telemetry.service.tls.trust-store-password:}") String trustStorePassword,
			@Value("${telemetry.service.tls.trust-store-type:PKCS12}") String trustStoreType,
			ResourceLoader resourceLoader) throws Exception {
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(connectTimeout)
				.sslContext(createSslContext(
						trustStoreLocation, trustStorePassword, trustStoreType, resourceLoader))
				.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(readTimeout);
		return RestClient.builder()
				.baseUrl(baseUrl)
				.requestFactory(requestFactory)
				.build();
	}

	private SSLContext createSslContext(
			String trustStoreLocation,
			String trustStorePassword,
			String trustStoreType,
			ResourceLoader resourceLoader) throws Exception {
		if (trustStoreLocation.isBlank()) {
			return SSLContext.getDefault();
		}

		KeyStore trustStore = KeyStore.getInstance(trustStoreType);
		Resource resource = resourceLoader.getResource(trustStoreLocation);
		try (InputStream input = resource.getInputStream()) {
			trustStore.load(input, trustStorePassword.toCharArray());
		}
		TrustManagerFactory trustManagers = TrustManagerFactory.getInstance(
				TrustManagerFactory.getDefaultAlgorithm());
		trustManagers.init(trustStore);

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustManagers.getTrustManagers(), null);
		return sslContext;
	}
}
