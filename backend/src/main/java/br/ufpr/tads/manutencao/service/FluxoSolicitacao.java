package br.ufpr.tads.manutencao.service;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import br.ufpr.tads.manutencao.model.EstadoSolicitacao;

public final class FluxoSolicitacao {

    private static final Map<EstadoSolicitacao, Set<EstadoSolicitacao>> TRANSOES_PERMITIDAS = new EnumMap<>(EstadoSolicitacao.class);

    static {
        TRANSOES_PERMITIDAS.put(EstadoSolicitacao.ABERTA, EnumSet.of(EstadoSolicitacao.ORCADA));
        TRANSOES_PERMITIDAS.put(EstadoSolicitacao.ORCADA, EnumSet.of(EstadoSolicitacao.APROVADA, EstadoSolicitacao.REJEITADA));
        TRANSOES_PERMITIDAS.put(EstadoSolicitacao.REJEITADA, EnumSet.of(EstadoSolicitacao.APROVADA));
        TRANSOES_PERMITIDAS.put(EstadoSolicitacao.APROVADA, EnumSet.of(EstadoSolicitacao.ARRUMADA, EstadoSolicitacao.REDIRECIONADA));
        TRANSOES_PERMITIDAS.put(EstadoSolicitacao.REDIRECIONADA, EnumSet.of(EstadoSolicitacao.ARRUMADA, EstadoSolicitacao.REDIRECIONADA));
        TRANSOES_PERMITIDAS.put(EstadoSolicitacao.ARRUMADA, EnumSet.of(EstadoSolicitacao.PAGA));
        TRANSOES_PERMITIDAS.put(EstadoSolicitacao.PAGA, EnumSet.of(EstadoSolicitacao.FINALIZADA));
        TRANSOES_PERMITIDAS.put(EstadoSolicitacao.FINALIZADA, Collections.emptySet());
    }

    private FluxoSolicitacao() {}

    public static boolean isTransicaoValida(EstadoSolicitacao anterior, EstadoSolicitacao novo) {
        if (novo == null) {
            return false;
        }
        if (anterior == null) {
            return novo == EstadoSolicitacao.ABERTA;
        }
        Set<EstadoSolicitacao> destinos = TRANSOES_PERMITIDAS.get(anterior);
        return destinos != null && destinos.contains(novo);
    }

    public static void validarTransicao(EstadoSolicitacao anterior, EstadoSolicitacao novo) {
        if (!isTransicaoValida(anterior, novo)) {
            throw new IllegalStateException(
                String.format("Transição de estado inválida: de %s para %s",
                    anterior != null ? anterior.name() : "NULO",
                    novo != null ? novo.name() : "NULO")
            );
        }
    }
}

