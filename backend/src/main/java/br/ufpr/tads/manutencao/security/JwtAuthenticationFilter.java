package br.ufpr.tads.manutencao.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtService.isTokenValido(token)) {
                try {
                    Claims claims = jwtService.extrairClaims(token);
                    Long usuarioId = Long.valueOf(claims.getSubject());
                    String email = claims.get("email", String.class);
                    String perfil = claims.get("perfil", String.class);

                    Number clienteIdNum = claims.get("clienteId", Number.class);
                    Long clienteId = clienteIdNum != null ? clienteIdNum.longValue() : null;

                    Number funcionarioIdNum = claims.get("funcionarioId", Number.class);
                    Long funcionarioId = funcionarioIdNum != null ? funcionarioIdNum.longValue() : null;

                    UsuarioAutenticado principal = new UsuarioAutenticado(
                            usuarioId, email, perfil, clienteId, funcionarioId);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            principal, null, principal.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } catch (Exception e) {
                    SecurityContextHolder.clearContext();
                }
            } else {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}

