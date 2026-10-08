package br.ufpr.tads.manutencao.dto;

import java.time.LocalDateTime;

public class HistoricoDTO {

    private String estado;
    private LocalDateTime dataHora;
    private String nomeFuncionario;

    public HistoricoDTO() {}

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getNomeFuncionario() {
        return nomeFuncionario;
    }

    public void setNomeFuncionario(String nomeFuncionario) {
        this.nomeFuncionario = nomeFuncionario;
    }
}
