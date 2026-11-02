package br.ufpr.tads.manutencao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.ufpr.tads.manutencao.model.EstadoSolicitacao;
import br.ufpr.tads.manutencao.model.Solicitacao;

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
}
