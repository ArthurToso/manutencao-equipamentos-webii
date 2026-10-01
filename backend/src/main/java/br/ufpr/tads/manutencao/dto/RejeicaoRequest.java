package br.ufpr.tads.manutencao.dto;

import jakarta.validation.constraints.NotBlank;

public class RejeicaoRequest {

    @NotBlank(message = "O motivo da rejeição é obrigatório")
    private String motivo;

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
