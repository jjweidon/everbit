package com.everbit.everbit.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(
		HttpSecurity http,
		JwtAuthenticationFilter jwtFilter,
		OAuth2AuthenticationSuccessHandler oauth2SuccessHandler,
		OAuth2AuthenticationFailureHandler oauth2FailureHandler,
		LogoutRefreshInvalidationHandler logoutRefreshInvalidationHandler,
		LogoutSuccessRedirectHandler logoutSuccessRedirectHandler
	) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
				.requestMatchers("/api/v2/oauth2/authorization/**", "/api/v2/login/oauth2/code/**").permitAll()
				.requestMatchers("/api/v2/auth/refresh").permitAll()
				.requestMatchers("/logout").permitAll()
				.anyRequest().authenticated()
			)
			.oauth2Login(oauth2 -> oauth2
				.authorizationEndpoint(authz -> authz.baseUri("/api/v2/oauth2/authorization"))
				.redirectionEndpoint(redir -> redir.baseUri("/api/v2/login/oauth2/code/*"))
				.successHandler(oauth2SuccessHandler)
				.failureHandler(oauth2FailureHandler)
			)
			.logout(logout -> logout
				.logoutUrl("/logout")
				.addLogoutHandler(logoutRefreshInvalidationHandler)
				.logoutSuccessHandler(logoutSuccessRedirectHandler)
			)
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint(new HttpStatusEntryPoint(UNAUTHORIZED))
			)
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
