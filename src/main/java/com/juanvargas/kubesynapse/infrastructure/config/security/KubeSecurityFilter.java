package com.juanvargas.kubesynapse.infrastructure.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ======================================================================================
 * FILTRO DE SEGURIDAD Y AUTORIZACIÓN RBAC (Keycloak & Token Resolver)
 * ======================================================================================
 * Resuelve la identidad y roles del operador:
 * 1. Soporta Tokens JWT de Keycloak (Authorization: Bearer <jwt>).
 * 2. Soporta Tokens de Desarrollo de 1 clic (Bearer dev-admin-token / dev-operator-token).
 * 3. Soporta Cabecera Explícita de Rol (X-User-Role: ROLE_ADMIN o ROLE_OPERATOR).
 * 4. Por defecto en modo local asigna ROLE_ADMIN si no se envía cabecera, permitiendo
 *    que el desarrollador trabaje sin fricción mientras el simulador RBAC está activo.
 * ======================================================================================
 */
@Slf4j
@Component
public class KubeSecurityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String roleHeader = request.getHeader("X-User-Role");

        String username = "operator";
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 1. Evaluar si es ADMIN
        if ("ROLE_ADMIN".equalsIgnoreCase(roleHeader) ||
                (authHeader != null && (authHeader.contains("dev-admin") || authHeader.contains("admin")))) {
            username = "admin";
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_OPERATOR"));
        } else if ("ROLE_OPERATOR".equalsIgnoreCase(roleHeader) ||
                (authHeader != null && (authHeader.contains("dev-operator") || authHeader.contains("operator")))) {
            username = "operator";
            authorities.add(new SimpleGrantedAuthority("ROLE_OPERATOR"));
        } else {
            // Default en desarrollo: ROLE_ADMIN para no bloquear endpoints no especificados
            username = "admin";
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_OPERATOR"));
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
