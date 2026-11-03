package com.juanvargas.kubesynapse.infrastructure.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * ======================================================================================
 * CONFIGURACIÓN DE SEGURIDAD Y CONTROL DE ACCESO BASADO EN ROLES (RBAC)
 * ======================================================================================
 * Protege los endpoints críticos de mutación en Kubernetes:
 * - ROLE_ADMIN: Mutaciones en el clúster (Remediaciones, Inyección de Caos CKA, Undo).
 * - ROLE_OPERATOR: Solo lectura y diagnóstico (Topología, Reportes IA, Verificación CKA).
 * ======================================================================================
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KubeSecurityFilter kubeSecurityFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) // H2 Console
                .authorizeHttpRequests(auth -> auth
                        // 1. Endpoints de mutación restringidos a ROLE_ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/remediations/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/cka/inject/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/cka/reset/**").hasAuthority("ROLE_ADMIN")

                        // 2. Endpoints de diagnóstico y CKA accesibles para OPERATOR y ADMIN
                        .requestMatchers("/api/v1/cka/exercises/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/cka/verify/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")
                        .requestMatchers("/api/v1/cluster/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")
                        .requestMatchers("/api/v1/diagnose/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")
                        .requestMatchers("/api/v1/audit/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")
                        .requestMatchers("/api/v1/agent/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")

                        // 3. Documentación Swagger, Actuator y Consola H2
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/**", "/h2-console/**").permitAll()

                        // 4. Todo el resto permitido
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");

                            Map<String, Object> errorBody = Map.of(
                                    "status", 403,
                                    "error", "FORBIDDEN",
                                    "message", "🔒 Acceso Denegado: Esta acción en Kubernetes requiere ROLE_ADMIN. El usuario actual posee permisos restringidos (ROLE_OPERATOR).",
                                    "timestamp", Instant.now().toString(),
                                    "path", request.getRequestURI()
                            );
                            response.getWriter().write(objectMapper.writeValueAsString(errorBody));
                        })
                )
                .addFilterBefore(kubeSecurityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "X-User-Role"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
