package br.ufpr.tads.manutencao.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioAuthDTO {

    private Long id;
    private String nome;
    private String email;
    private String perfil;
    private Long clienteId;
    private Long funcionarioId;

    public UsuarioAuthDTO() {}

    public UsuarioAuthDTO(Long id, String nome, String email, String perfil, Long clienteId, Long funcionarioId) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.clienteId = clienteId;
        this.funcionarioId = funcionarioId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getFuncionarioId() {
        return funcionarioId;
    }

    public void setFuncionarioId(Long funcionarioId) {
        this.funcionarioId = funcionarioId;
    }
}

