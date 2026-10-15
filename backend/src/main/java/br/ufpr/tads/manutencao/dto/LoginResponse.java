package br.ufpr.tads.manutencao.dto;

public class LoginResponse {

    private String token;
    private String tipo = "Bearer";
    private Long expiraEm;
    private UsuarioAuthDTO usuario;

    public LoginResponse() {}

    public LoginResponse(String token, String tipo, Long expiraEm, UsuarioAuthDTO usuario) {
        this.token = token;
        this.tipo = tipo;
        this.expiraEm = expiraEm;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(Long expiraEm) {
        this.expiraEm = expiraEm;
    }

    public UsuarioAuthDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioAuthDTO usuario) {
        this.usuario = usuario;
    }
}

