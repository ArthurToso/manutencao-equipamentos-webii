package br.ufpr.tads.manutencao.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReceitaDiaResponse {

    private LocalDate data;
    private BigDecimal receita;

    public ReceitaDiaResponse(LocalDate data, BigDecimal receita) {
        this.data = data;
        this.receita = receita;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public BigDecimal getReceita() {
        return receita;
    }

    public void setReceita(BigDecimal receita) {
        this.receita = receita;
    }
}
