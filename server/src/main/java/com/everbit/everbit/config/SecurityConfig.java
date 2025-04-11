package com.everbit.everbit.config;

import com.everbit.everbit.jwt.JwtFilter;
import com.everbit.everbit.jwt.JwtUtil;
import com.everbit.everbit.oauth2.CustomOAuth2UserService;
import com.everbit.everbit.oauth2.CustomSuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                        .authorizationEndpoint(endpoint -> {
                            endpoint.baseUri("/api/login");
                            log.info("OAuth2 인증 엔드포인트 설정: /api/login");
                        })
                        .redirectionEndpoint(endpoint -> {
                            endpoint.baseUri("/api/login/oauth2/code/*");
                            log.info("OAuth2 리다이렉션 엔드포인트 설정: /api/login/oauth2/code/*");
                        })
                        .userInfoEndpoint(userInfo -> 
                            userInfo.userService(customOAuth2UserService)
                        )
                        .successHandler(customSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 인증 실패: {}", exception.getMessage());
                            exception.printStackTrace();
                            response.sendRedirect("/api/oauth2/error/debug?error=" + exception.getMessage());
                        })
                );
        
        // 인증 없이 접근 가능한 경로 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", 
                                "/api/login/**", 
                                "/api/oauth2/**", 
                                "/api/login/oauth2/code/**",
                                "/api/auth/me",
                                "/favicon.ico",
                                "/error").permitAll()
                .anyRequest().authenticated()
        );

        return http.build();
    }
} 