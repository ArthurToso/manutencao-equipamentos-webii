package br.ufpr.tads.manutencao.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.ufpr.tads.manutencao.dto.ReceitaCategoriaResponse;
import br.ufpr.tads.manutencao.dto.ReceitaDiaResponse;
import br.ufpr.tads.manutencao.model.HistoricoSolicitacao;

@Repository
public interface HistoricoSolicitacaoRepository extends JpaRepository<HistoricoSolicitacao, Long> {

    @Query("""
            SELECT h FROM HistoricoSolicitacao h
            JOIN FETCH h.usuario
            LEFT JOIN FETCH h.usuarioDestino
            WHERE h.solicitacao.id = :solicitacaoId
            ORDER BY h.dataHora ASC, h.id ASC
            """)
    List<HistoricoSolicitacao> findBySolicitacaoIdOrderByDataHoraAsc(@Param("solicitacaoId") Long solicitacaoId);

    @Query("""
            SELECT h FROM HistoricoSolicitacao h
            JOIN FETCH h.usuario
            LEFT JOIN FETCH h.usuarioDestino
            WHERE h.solicitacao.id = :solicitacaoId
            ORDER BY h.dataHora DESC, h.id DESC
            """)
    List<HistoricoSolicitacao> findUltimoPorSolicitacaoIdInternal(@Param("solicitacaoId") Long solicitacaoId, Pageable pageable);

    default Optional<HistoricoSolicitacao> findUltimoPorSolicitacaoId(Long solicitacaoId) {
        List<HistoricoSolicitacao> list = findUltimoPorSolicitacaoIdInternal(solicitacaoId, PageRequest.of(0, 1));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Query("""
            SELECT new br.ufpr.tads.manutencao.dto.ReceitaDiaResponse(CAST(h.dataHora AS LocalDate), SUM(s.valorOrcamento))
            FROM HistoricoSolicitacao h
            JOIN h.solicitacao s
            WHERE h.estado = br.ufpr.tads.manutencao.model.EstadoSolicitacao.PAGA
              AND h.dataHora >= :inicio
              AND h.dataHora < :fim
            GROUP BY CAST(h.dataHora AS LocalDate)
            ORDER BY CAST(h.dataHora AS LocalDate)
            """)
    List<ReceitaDiaResponse> somarReceitasPorDia(@Param("inicio") LocalDateTime inicio,
                                                 @Param("fim") LocalDateTime fim);

    @Query("""
            SELECT new br.ufpr.tads.manutencao.dto.ReceitaCategoriaResponse(c.nome, SUM(s.valorOrcamento))
            FROM HistoricoSolicitacao h
            JOIN h.solicitacao s
            JOIN s.categoria c
            WHERE h.estado = br.ufpr.tads.manutencao.model.EstadoSolicitacao.PAGA
            GROUP BY c.id, c.nome
            ORDER BY SUM(s.valorOrcamento) DESC
            """)
    List<ReceitaCategoriaResponse> somarReceitasPorCategoria();
}
