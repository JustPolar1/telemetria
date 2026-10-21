package com.telemetria.basica.parcel.security;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration(proxyBeanMethods = false)
public class JwtConfiguration {

	@Bean
	SecretKey jwtSecretKey(@Value("${security.jwt.secret-base64}") String secretBase64) {
		byte[] keyBytes;
		try {
			keyBytes = Base64.getDecoder().decode(secretBase64);
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("security.jwt.secret-base64 must be valid Base64", exception);
		}
		if (keyBytes.length < 32) {
			throw new IllegalStateException("The JWT HMAC key must contain at least 256 bits");
		}
		return new SecretKeySpec(keyBytes, "HmacSHA256");
	}

	@Bean
	JwtDecoder jwtDecoder(
			SecretKey jwtSecretKey,
			@Value("${security.jwt.issuer}") String issuer) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
		return decoder;
	}
}
