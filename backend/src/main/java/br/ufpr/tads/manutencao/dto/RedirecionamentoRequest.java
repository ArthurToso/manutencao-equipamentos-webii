package br.ufpr.tads.manutencao.dto;

import jakarta.validation.constraints.NotNull;

public class RedirecionamentoRequest {

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
