package br.ufpr.tads.manutencao.security;

import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<UsuarioAutenticado> getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado ua) {
            return Optional.of(ua);
        }
        return Optional.empty();
    }

    public static UsuarioAutenticado getUsuarioAutenticadoObrigatorio() {
        return getUsuarioAutenticado()
                .orElseThrow(() -> new AccessDeniedException("Usuário não autenticado"));
    }
}

