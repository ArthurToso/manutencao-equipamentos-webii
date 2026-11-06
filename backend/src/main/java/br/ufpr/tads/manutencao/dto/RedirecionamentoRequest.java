package br.ufpr.tads.manutencao.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotNull;

public class RedirecionamentoRequest {

    @JsonAlias("funcionarioId")
    @NotNull(message = "O funcionário de destino é obrigatório")
    private Long funcionarioDestinoId;

    public RedirecionamentoRequest() {}

    public RedirecionamentoRequest(Long funcionarioDestinoId) {
        this.funcionarioDestinoId = funcionarioDestinoId;
    }

    public Long getFuncionarioDestinoId() {
        return funcionarioDestinoId;
    }

    public void setFuncionarioDestinoId(Long funcionarioDestinoId) {
        this.funcionarioDestinoId = funcionarioDestinoId;
    }
}
