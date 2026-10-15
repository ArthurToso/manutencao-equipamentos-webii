package br.ufpr.tads.manutencao.dto;

import java.math.BigDecimal;

public class ReceitaCategoriaResponse {

    private String categoria;
    private BigDecimal receita;

    public ReceitaCategoriaResponse(String categoria, BigDecimal receita) {
        this.categoria = categoria;
        this.receita = receita;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getReceita() {
        return receita;
    }

    public void setReceita(BigDecimal receita) {
        this.receita = receita;
    }
}
