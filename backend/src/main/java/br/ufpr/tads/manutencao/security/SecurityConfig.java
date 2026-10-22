package br.ufpr.tads.manutencao.security;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/autocadastro").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/clientes").permitAll()
                .requestMatchers("/api/funcionarios/**").hasRole("FUNCIONARIO")
                .requestMatchers("/api/relatorios/**").hasRole("FUNCIONARIO")
                .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/categorias/**").hasAnyRole("CLIENTE", "FUNCIONARIO")
                .requestMatchers(HttpMethod.POST, "/api/categorias", "/api/categorias/**").hasRole("FUNCIONARIO")
                .requestMatchers(HttpMethod.PUT, "/api/categorias", "/api/categorias/**").hasRole("FUNCIONARIO")
                .requestMatchers(HttpMethod.DELETE, "/api/categorias", "/api/categorias/**").hasRole("FUNCIONARIO")
                .requestMatchers(HttpMethod.POST, "/api/solicitacoes").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.GET, "/api/solicitacoes/minhas").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.GET, "/api/solicitacoes/abertas").hasRole("FUNCIONARIO")
                .requestMatchers(HttpMethod.POST, "/api/solicitacoes/*/orcamento").hasRole("FUNCIONARIO")
                .requestMatchers(HttpMethod.PUT, "/api/solicitacoes/*/orcamento").hasRole("FUNCIONARIO")
                .requestMatchers(HttpMethod.PUT, "/api/solicitacoes/*/aprovar").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.PUT, "/api/solicitacoes/*/rejeitar").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.GET, "/api/solicitacoes/*/orcamento").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.GET, "/api/solicitacoes/*").hasAnyRole("CLIENTE", "FUNCIONARIO")
                .requestMatchers("/api/solicitacoes/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now().toString());
            body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            body.put("erro", "Não Autorizado");
            body.put("mensagem", "Token de autenticação ausente, expirado ou inválido");

            objectMapper.writeValue(response.getOutputStream(), body);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now().toString());
            body.put("status", HttpServletResponse.SC_FORBIDDEN);
            body.put("erro", "Acesso Proibido");
            body.put("mensagem", "Você não tem permissão para acessar este recurso");

            objectMapper.writeValue(response.getOutputStream(), body);
        };
    }
}

