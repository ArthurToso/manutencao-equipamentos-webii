package br.ufpr.tads.manutencao.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.ufpr.tads.manutencao.dto.ReceitaCategoriaResponse;
import br.ufpr.tads.manutencao.dto.ReceitaDiaResponse;
import br.ufpr.tads.manutencao.model.HistoricoSolicitacao;

@Repository
public interface HistoricoSolicitacaoRepository extends JpaRepository<HistoricoSolicitacao, Long> {

    List<HistoricoSolicitacao> findBySolicitacaoIdOrderByDataHoraAsc(Long solicitacaoId);

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
