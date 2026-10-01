package br.ufpr.tads.manutencao.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ufpr.tads.manutencao.model.Solicitacao;

import java.util.List;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
    List<Solicitacao> findByClienteIdOrderByDataHoraDesc(Long clienteId);
}
