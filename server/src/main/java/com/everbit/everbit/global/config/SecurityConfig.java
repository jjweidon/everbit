package com.everbit.everbit.global.config;

import com.everbit.everbit.global.jwt.JwtFilter;
import com.everbit.everbit.global.jwt.JwtUtil;
import com.everbit.everbit.oauth2.service.CustomOAuth2UserService;
import com.everbit.everbit.oauth2.service.CustomSuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JwtUtil jwtUtil;
    private final CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // OAuth2 로그인 설정
        http
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> 
                            endpoint.baseUri("/api/login")
                        )
                        .userInfoEndpoint(userInfo -> 
                            userInfo.userService(customOAuth2UserService)
                        )
                        .successHandler(customSuccessHandler)
                );
        
        // 인증 없이 접근 가능한 경로 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/login/**",
                               "/api/oauth2/code/**",
                               "/api/login/oauth2/code/**",
                               "/api/auth/me").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/members/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/members/upbit-keys").authenticated()
                .requestMatchers("/api/members/**").authenticated()
                .anyRequest().authenticated()
        );

        return http.build();
    }
} 