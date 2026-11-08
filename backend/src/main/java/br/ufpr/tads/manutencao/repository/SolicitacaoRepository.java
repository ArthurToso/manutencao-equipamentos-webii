package br.ufpr.tads.manutencao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.ufpr.tads.manutencao.dto.SolicitacaoResponse;
import br.ufpr.tads.manutencao.model.EstadoSolicitacao;
import br.ufpr.tads.manutencao.model.Solicitacao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
    List<Solicitacao> findByClienteIdOrderByDataHoraAsc(Long clienteId);
    List<Solicitacao> findByEstadoOrderByDataHoraAsc(EstadoSolicitacao estado);

    @Override
    @Query("SELECT s FROM Solicitacao s JOIN FETCH s.categoria WHERE s.id = :id")
    Optional<Solicitacao> findById(@Param("id") Long id);

    @Query("SELECT s FROM Solicitacao s JOIN FETCH s.categoria WHERE s.id = :id")
    Optional<Solicitacao> findByIdWithCategoria(@Param("id") Long id);

    @Query("""
            SELECT new br.ufpr.tads.manutencao.dto.SolicitacaoResponse(
                s.id, s.dataHora, s.descricaoEquipamento, s.descricaoDefeito, s.estado,
                cat.id, cat.nome, s.clienteId, c.nome, s.valorOrcamento, s.motivoRejeicao
            )
            FROM Solicitacao s
            JOIN s.categoria cat
            JOIN Cliente c ON c.id = s.clienteId
            WHERE (s.estado <> br.ufpr.tads.manutencao.model.EstadoSolicitacao.REDIRECIONADA
                   OR EXISTS (
                       SELECT 1 FROM HistoricoSolicitacao h
                       WHERE h.solicitacao = s
                         AND h.usuarioDestino.id = :usuarioId
                         AND h.id = (
                             SELECT MAX(h2.id)
                             FROM HistoricoSolicitacao h2
                             WHERE h2.solicitacao = s
                         )
                   ))
            ORDER BY s.dataHora ASC, s.id ASC
            """)
    List<SolicitacaoResponse> listarTodasParaFuncionario(@Param("usuarioId") Long usuarioId);

    @Query("""
            SELECT new br.ufpr.tads.manutencao.dto.SolicitacaoResponse(
                s.id, s.dataHora, s.descricaoEquipamento, s.descricaoDefeito, s.estado,
                cat.id, cat.nome, s.clienteId, c.nome, s.valorOrcamento, s.motivoRejeicao
            )
            FROM Solicitacao s
            JOIN s.categoria cat
            JOIN Cliente c ON c.id = s.clienteId
            WHERE (s.estado <> br.ufpr.tads.manutencao.model.EstadoSolicitacao.REDIRECIONADA
                   OR EXISTS (
                       SELECT 1 FROM HistoricoSolicitacao h
                       WHERE h.solicitacao = s
                         AND h.usuarioDestino.id = :usuarioId
                         AND h.id = (
                             SELECT MAX(h2.id)
                             FROM HistoricoSolicitacao h2
                             WHERE h2.solicitacao = s
                         )
                   ))
              AND s.dataHora >= :dataHoraInicio
              AND s.dataHora < :dataHoraFim
            ORDER BY s.dataHora ASC, s.id ASC
            """)
    List<SolicitacaoResponse> listarPorPeriodoParaFuncionario(@Param("usuarioId") Long usuarioId,
                                                             @Param("dataHoraInicio") LocalDateTime dataHoraInicio,
                                                             @Param("dataHoraFim") LocalDateTime dataHoraFim);

    @Query("""
            SELECT new br.ufpr.tads.manutencao.dto.SolicitacaoResponse(
                s.id, s.dataHora, s.descricaoEquipamento, s.descricaoDefeito, s.estado,
                cat.id, cat.nome, s.clienteId, c.nome, s.valorOrcamento, s.motivoRejeicao
            )
            FROM Solicitacao s
            JOIN s.categoria cat
            JOIN Cliente c ON c.id = s.clienteId
            WHERE (s.estado <> br.ufpr.tads.manutencao.model.EstadoSolicitacao.REDIRECIONADA
                   OR EXISTS (
                       SELECT 1 FROM HistoricoSolicitacao h
                       WHERE h.solicitacao = s
                         AND h.usuarioDestino.id = :usuarioId
                         AND h.id = (
                             SELECT MAX(h2.id)
                             FROM HistoricoSolicitacao h2
                             WHERE h2.solicitacao = s
                         )
                   ))
              AND s.dataHora >= :dataHoraInicio
            ORDER BY s.dataHora ASC, s.id ASC
            """)
    List<SolicitacaoResponse> listarAposDataParaFuncionario(@Param("usuarioId") Long usuarioId,
                                                           @Param("dataHoraInicio") LocalDateTime dataHoraInicio);

    @Query("""
            SELECT new br.ufpr.tads.manutencao.dto.SolicitacaoResponse(
                s.id, s.dataHora, s.descricaoEquipamento, s.descricaoDefeito, s.estado,
                cat.id, cat.nome, s.clienteId, c.nome, s.valorOrcamento, s.motivoRejeicao
            )
            FROM Solicitacao s
            JOIN s.categoria cat
            JOIN Cliente c ON c.id = s.clienteId
            WHERE (s.estado <> br.ufpr.tads.manutencao.model.EstadoSolicitacao.REDIRECIONADA
                   OR EXISTS (
                       SELECT 1 FROM HistoricoSolicitacao h
                       WHERE h.solicitacao = s
                         AND h.usuarioDestino.id = :usuarioId
                         AND h.id = (
                             SELECT MAX(h2.id)
                             FROM HistoricoSolicitacao h2
                             WHERE h2.solicitacao = s
                         )
                   ))
              AND s.dataHora < :dataHoraFim
            ORDER BY s.dataHora ASC, s.id ASC
            """)
    List<SolicitacaoResponse> listarAteDataParaFuncionario(@Param("usuarioId") Long usuarioId,
                                                          @Param("dataHoraFim") LocalDateTime dataHoraFim);
}
