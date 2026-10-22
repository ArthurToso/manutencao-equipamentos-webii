package br.ufpr.tads.manutencao.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.ufpr.tads.manutencao.model.EstadoSolicitacao;

class FluxoSolicitacaoTest {

    @Test
    @DisplayName("Criação: estado anterior nulo deve permitir apenas ABERTA")
    void criacaoPermiteApenasAberta() {
        assertTrue(FluxoSolicitacao.isTransicaoValida(null, EstadoSolicitacao.ABERTA));
        assertDoesNotThrow(() -> FluxoSolicitacao.validarTransicao(null, EstadoSolicitacao.ABERTA));

        assertFalse(FluxoSolicitacao.isTransicaoValida(null, EstadoSolicitacao.ORCADA));
        assertFalse(FluxoSolicitacao.isTransicaoValida(null, EstadoSolicitacao.APROVADA));
        assertThrows(IllegalStateException.class, () -> FluxoSolicitacao.validarTransicao(null, EstadoSolicitacao.ORCADA));
    }

    @Test
    @DisplayName("ABERTA só pode ir para ORCADA")
    void abertaParaOrcada() {
        assertTrue(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.ABERTA, EstadoSolicitacao.ORCADA));
        assertFalse(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.ABERTA, EstadoSolicitacao.APROVADA));
        assertFalse(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.ABERTA, EstadoSolicitacao.PAGA));
        assertThrows(IllegalStateException.class, () -> FluxoSolicitacao.validarTransicao(EstadoSolicitacao.ABERTA, EstadoSolicitacao.PAGA));
    }

    @Test
    @DisplayName("ORCADA pode ir para APROVADA ou REJEITADA")
    void orcadaParaAprovadaOuRejeitada() {
        assertTrue(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.ORCADA, EstadoSolicitacao.APROVADA));
        assertTrue(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.ORCADA, EstadoSolicitacao.REJEITADA));
        assertFalse(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.ORCADA, EstadoSolicitacao.ARRUMADA));
    }

    @Test
    @DisplayName("RF009: Resgate REJEITADA -> APROVADA é aceito pelo fluxo")
    void resgateRejeitadaParaAprovada() {
        assertTrue(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.REJEITADA, EstadoSolicitacao.APROVADA));
        assertDoesNotThrow(() -> FluxoSolicitacao.validarTransicao(EstadoSolicitacao.REJEITADA, EstadoSolicitacao.APROVADA));

        assertFalse(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.REJEITADA, EstadoSolicitacao.ARRUMADA));
        assertFalse(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.REJEITADA, EstadoSolicitacao.ORCADA));
    }

    @Test
    @DisplayName("APROVADA pode ir para ARRUMADA ou REDIRECIONADA")
    void aprovadaParaArrumadaOuRedirecionada() {
        assertTrue(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.APROVADA, EstadoSolicitacao.ARRUMADA));
        assertTrue(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.APROVADA, EstadoSolicitacao.REDIRECIONADA));
        assertFalse(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.APROVADA, EstadoSolicitacao.PAGA));
    }

    @Test
    @DisplayName("RF015: REDIRECIONADA pode ir para ARRUMADA ou REDIRECIONADA (infinitas vezes)")
    void redirecionadaParaArrumadaOuRedirecionada() {
        assertTrue(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.REDIRECIONADA, EstadoSolicitacao.ARRUMADA));
        assertTrue(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.REDIRECIONADA, EstadoSolicitacao.REDIRECIONADA));
        assertDoesNotThrow(() -> FluxoSolicitacao.validarTransicao(EstadoSolicitacao.REDIRECIONADA, EstadoSolicitacao.REDIRECIONADA));
        assertFalse(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.REDIRECIONADA, EstadoSolicitacao.FINALIZADA));
    }

    @Test
    @DisplayName("ARRUMADA só pode ir para PAGA")
    void arrumadaParaPaga() {
        assertTrue(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.ARRUMADA, EstadoSolicitacao.PAGA));
        assertFalse(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.ARRUMADA, EstadoSolicitacao.FINALIZADA));
    }

    @Test
    @DisplayName("PAGA só pode ir para FINALIZADA")
    void pagaParaFinalizada() {
        assertTrue(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.PAGA, EstadoSolicitacao.FINALIZADA));
        assertFalse(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.PAGA, EstadoSolicitacao.ARRUMADA));
    }

    @Test
    @DisplayName("FINALIZADA é terminal e não permite transição para nenhum estado")
    void finalizadaETerminal() {
        for (EstadoSolicitacao destino : EstadoSolicitacao.values()) {
            assertFalse(FluxoSolicitacao.isTransicaoValida(EstadoSolicitacao.FINALIZADA, destino));
            assertThrows(IllegalStateException.class, () -> FluxoSolicitacao.validarTransicao(EstadoSolicitacao.FINALIZADA, destino));
        }
    }
}

