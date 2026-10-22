package br.ufpr.tads.manutencao.dto;

import java.time.LocalDateTime;

public class HistoricoDTO {

    private Long id;
    private String estadoAnterior;
    private String estado;
    private LocalDateTime dataHora;
    private String nomeFuncionario;
    private UsuarioResumoDTO responsavel;
    private FuncionarioDestinoDTO funcionarioDestino;
    private String observacao;

    public HistoricoDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(String estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

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

    public UsuarioResumoDTO getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(UsuarioResumoDTO responsavel) {
        this.responsavel = responsavel;
    }

    public FuncionarioDestinoDTO getFuncionarioDestino() {
        return funcionarioDestino;
    }

    public void setFuncionarioDestino(FuncionarioDestinoDTO funcionarioDestino) {
        this.funcionarioDestino = funcionarioDestino;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
