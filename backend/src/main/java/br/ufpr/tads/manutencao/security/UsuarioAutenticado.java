package br.ufpr.tads.manutencao.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UsuarioAutenticado implements UserDetails, Serializable {

    private final Long id;
    private final String email;
    private final String perfil;
    private final Long clienteId;
    private final Long funcionarioId;
    private final List<SimpleGrantedAuthority> authorities;

    public UsuarioAutenticado(Long id, String email, String perfil, Long clienteId, Long funcionarioId) {
        this.id = id;
        this.email = email;
        this.perfil = perfil;
        this.clienteId = clienteId;
        this.funcionarioId = funcionarioId;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + perfil));
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPerfil() {
        return perfil;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public Long getFuncionarioId() {
        return funcionarioId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

