package br.ufpr.tads.manutencao.dto;

public class UsuarioResumoDTO {

    private Long id;
    private String nome;
    private String perfil;

    public UsuarioResumoDTO() {}

    public UsuarioResumoDTO(Long id, String nome, String perfil) {
        this.id = id;
        this.nome = nome;
        this.perfil = perfil;
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

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }
}

