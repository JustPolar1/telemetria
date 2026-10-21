package com.telemetria.basica.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetailsService;

class AuthBeansConfigurationTests {

	@Test
	void storesConfiguredCredentialAsBcryptHash() {
		AuthBeansConfiguration configuration = new AuthBeansConfiguration();
		String rawPassword = "test-password";
		String encodedPassword = configuration.passwordEncoder().encode(rawPassword);
		UserDetailsService users = configuration.userDetailsService("test-user", encodedPassword);
		var storedUser = users.loadUserByUsername("test-user");

		assertEquals(encodedPassword, storedUser.getPassword());
		assertTrue(configuration.passwordEncoder().matches(rawPassword, storedUser.getPassword()));
	}
}
