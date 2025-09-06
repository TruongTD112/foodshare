package com.miniapp.foodshare.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Order(1)
public class SecurityConfig {
	private final EndpointAuthFilter endpointAuthFilter;
	private final RequestResponseLoggingFilter requestResponseLoggingFilter;

	public SecurityConfig(EndpointAuthFilter endpointAuthFilter, RequestResponseLoggingFilter requestResponseLoggingFilter) {
		this.endpointAuthFilter = endpointAuthFilter;
		this.requestResponseLoggingFilter = requestResponseLoggingFilter;
	}

	@Bean
	@Order(1)
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/auth/**", "/health-check").permitAll()
				.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
				.anyRequest().permitAll()
			)
			.addFilterBefore(requestResponseLoggingFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(endpointAuthFilter, UsernamePasswordAuthenticationFilter.class)
			.httpBasic(Customizer.withDefaults());
		return http.build();
	}
}
