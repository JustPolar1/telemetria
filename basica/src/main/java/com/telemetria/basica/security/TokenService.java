package com.telemetria.basica.security;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

	private final AuthenticationManager authenticationManager;
	private final JwtEncoder jwtEncoder;
	private final String issuer;
	private final Duration tokenTtl;

	public TokenService(
			AuthenticationManager authenticationManager,
			JwtEncoder jwtEncoder,
			@Value("${security.jwt.issuer}") String issuer,
			@Value("${security.jwt.ttl:PT15M}") Duration tokenTtl) {
		if (issuer.isBlank()) {
			throw new IllegalStateException("security.jwt.issuer must not be blank");
		}
		if (tokenTtl.isZero() || tokenTtl.isNegative()) {
			throw new IllegalStateException("security.jwt.ttl must be positive");
		}
		this.authenticationManager = authenticationManager;
		this.jwtEncoder = jwtEncoder;
		this.issuer = issuer;
		this.tokenTtl = tokenTtl;
	}

	public AuthResponse issueToken(AuthRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(tokenTtl);
		List<String> roles = authentication.getAuthorities().stream()
				.map(authority -> authority.getAuthority())
				.toList();

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(issuer)
				.subject(authentication.getName())
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.claim("roles", roles)
				.build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

		return new AuthResponse(token, "Bearer", tokenTtl.toSeconds());
	}
}
