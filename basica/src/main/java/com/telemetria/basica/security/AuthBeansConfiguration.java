package com.telemetria.basica.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration(proxyBeanMethods = false)
public class AuthBeansConfiguration {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService(
			@Value("${security.auth.username}") String username,
			@Value("${security.auth.password-hash}") String passwordHash) {
		if (username.isBlank()) {
			throw new IllegalStateException("security.auth.username must not be blank");
		}
		if (!passwordHash.matches("^\\$2[aby]\\$\\d\\d\\$[./A-Za-z0-9]{53}$")) {
			throw new IllegalStateException("security.auth.password-hash must be a BCrypt hash");
		}
		return new InMemoryUserDetailsManager(User.withUsername(username)
				.password(passwordHash)
				.roles("USER")
				.build());
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}
}
