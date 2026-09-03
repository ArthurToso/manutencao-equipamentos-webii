package br.ufpr.tads.manutencao.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ufpr.tads.manutencao.model.Solicitacao;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
}
