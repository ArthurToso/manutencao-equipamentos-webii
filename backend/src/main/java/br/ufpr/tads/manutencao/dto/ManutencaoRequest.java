package br.ufpr.tads.manutencao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ManutencaoRequest {

    @NotBlank(message = "A descrição da manutenção é obrigatória")
    @Size(max = 1000, message = "A descrição da manutenção deve ter no máximo 1000 caracteres")
    private String descricaoManutencao;

    @NotBlank(message = "As orientações ao cliente são obrigatórias")
    @Size(max = 1000, message = "As orientações ao cliente devem ter no máximo 1000 caracteres")
    private String orientacoesCliente;

    public ManutencaoRequest() {}

    public String getDescricaoManutencao() {
        return descricaoManutencao;
    }

    public void setDescricaoManutencao(String descricaoManutencao) {
        this.descricaoManutencao = descricaoManutencao;
    }

    public String getOrientacoesCliente() {
        return orientacoesCliente;
    }

    public void setOrientacoesCliente(String orientacoesCliente) {
        this.orientacoesCliente = orientacoesCliente;
    }
}
