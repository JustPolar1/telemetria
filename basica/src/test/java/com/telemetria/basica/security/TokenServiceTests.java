package com.telemetria.basica.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

class TokenServiceTests {

	private static final String ISSUER = "telemetry-service";
	private static final Duration TOKEN_TTL = Duration.ofMinutes(15);
	private static final SecretKey SECRET_KEY = new SecretKeySpec(new byte[32], "HmacSHA256");

	private AuthenticationManager authenticationManager;
	private JwtEncoder jwtEncoder;
	private JwtDecoder jwtDecoder;
	private TokenService tokenService;

	@BeforeEach
	void setUp() {
		authenticationManager = mock(AuthenticationManager.class);
		jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(SECRET_KEY));
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(SECRET_KEY)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
		jwtDecoder = decoder;
		tokenService = new TokenService(authenticationManager, jwtEncoder, ISSUER, TOKEN_TTL);
	}

	@Test
	void issuesSignedTokenWithIdentityRoleIssuerAndExpiry() {
		when(authenticationManager.authenticate(any())).thenReturn(
				UsernamePasswordAuthenticationToken.authenticated(
						"test-user", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		AuthResponse response = tokenService.issueToken(new AuthRequest("test-user", "password"));

		var jwt = jwtDecoder.decode(response.accessToken());
		assertEquals("Bearer", response.tokenType());
		assertEquals(TOKEN_TTL.toSeconds(), response.expiresIn());
		assertEquals("test-user", jwt.getSubject());
		assertEquals(ISSUER, jwt.getClaimAsString("iss"));
		assertEquals(List.of("ROLE_USER"), jwt.getClaimAsStringList("roles"));
		assertEquals(TOKEN_TTL.getSeconds(), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()).toSeconds());
	}

	@Test
	void doesNotIssueTokenWhenCredentialsAreRejected() {
		when(authenticationManager.authenticate(any()))
				.thenThrow(new BadCredentialsException("Bad credentials"));

		assertThrows(BadCredentialsException.class,
				() -> tokenService.issueToken(new AuthRequest("test-user", "wrong-password")));
	}

	@Test
	void decoderRejectsExpiredTokens() {
		Instant now = Instant.now();
		JwtClaimsSet expiredClaims = JwtClaimsSet.builder()
				.issuer(ISSUER)
				.subject("test-user")
				.issuedAt(now.minusSeconds(120))
				.expiresAt(now.minusSeconds(60))
				.build();
		String expiredToken = jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(), expiredClaims)).getTokenValue();

		assertThrows(JwtException.class, () -> jwtDecoder.decode(expiredToken));
	}

	@Test
	void decoderRejectsTokensFromAnotherIssuer() {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("untrusted-issuer")
				.subject("test-user")
				.issuedAt(now)
				.expiresAt(now.plusSeconds(60))
				.build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

		assertThrows(JwtException.class, () -> jwtDecoder.decode(token));
	}
}
