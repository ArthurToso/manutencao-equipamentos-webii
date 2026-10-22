package br.ufpr.tads.manutencao.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.tads.manutencao.model.EstadoSolicitacao;
import br.ufpr.tads.manutencao.model.HistoricoSolicitacao;
import br.ufpr.tads.manutencao.model.Perfil;
import br.ufpr.tads.manutencao.model.Solicitacao;
import br.ufpr.tads.manutencao.model.Usuario;
import br.ufpr.tads.manutencao.repository.HistoricoSolicitacaoRepository;

@Service
public class HistoricoSolicitacaoService {

    private final HistoricoSolicitacaoRepository historicoSolicitacaoRepository;

    public HistoricoSolicitacaoService(HistoricoSolicitacaoRepository historicoSolicitacaoRepository) {
        this.historicoSolicitacaoRepository = historicoSolicitacaoRepository;
    }

    @Transactional
    public HistoricoSolicitacao registrar(Solicitacao s, EstadoSolicitacao anterior, EstadoSolicitacao novo,
                                          Usuario responsavel, Usuario destino, String observacao) {
        LocalDateTime dataHora = (s != null && s.getDataHora() != null) ? s.getDataHora() : LocalDateTime.now();
        return registrar(s, anterior, novo, responsavel, destino, observacao, dataHora);
    }

    @Transactional
    public HistoricoSolicitacao registrar(Solicitacao s, EstadoSolicitacao anterior, EstadoSolicitacao novo,
                                          Usuario responsavel, Usuario destino, String observacao,
                                          LocalDateTime dataHora) {
        if (s == null) {
            throw new IllegalArgumentException("Solicitação é obrigatória");
        }
        if (novo == null) {
            throw new IllegalArgumentException("Novo estado é obrigatório");
        }
        if (responsavel == null) {
            throw new IllegalArgumentException("Usuário responsável é obrigatório");
        }

        if (anterior == null) {
            if (novo != EstadoSolicitacao.ABERTA) {
                throw new IllegalStateException("Estado anterior nulo só é permitido para criação no estado ABERTA");
            }
        } else {
            if (novo == EstadoSolicitacao.ABERTA) {
                throw new IllegalStateException("Não é permitido transitar para o estado ABERTA");
            }
        }

        if (novo == EstadoSolicitacao.REDIRECIONADA) {
            if (destino == null) {
                throw new IllegalArgumentException("Funcionário destino é obrigatório no redirecionamento");
            }
            if (destino.getId() != null && responsavel.getId() != null && destino.getId().equals(responsavel.getId())) {
                throw new IllegalArgumentException("Funcionário destino não pode ser o mesmo funcionário responsável");
            }
            if (destino.getPerfil() != Perfil.FUNCIONARIO) {
                throw new IllegalArgumentException("Funcionário destino deve ser do perfil FUNCIONARIO");
            }
            if (!destino.isAtivo()) {
                throw new IllegalStateException("Funcionário destino deve estar ativo");
            }
        } else {
            if (destino != null) {
                throw new IllegalArgumentException("Funcionário destino só deve ser informado quando o estado for REDIRECIONADA");
            }
        }

        if (novo == EstadoSolicitacao.REJEITADA) {
            if (observacao == null || observacao.trim().isEmpty()) {
                throw new IllegalArgumentException("O motivo da rejeição é obrigatório");
            }
        }

        HistoricoSolicitacao historico = new HistoricoSolicitacao(
                s,
                anterior,
                novo,
                dataHora != null ? dataHora : LocalDateTime.now(),
                responsavel,
                destino,
                observacao
        );

        return historicoSolicitacaoRepository.save(historico);
    }

    @Transactional(readOnly = true)
    public List<HistoricoSolicitacao> listar(Long solicitacaoId) {
        if (solicitacaoId == null) {
            throw new IllegalArgumentException("ID da solicitação é obrigatório");
        }
        return historicoSolicitacaoRepository.findBySolicitacaoIdOrderByDataHoraAsc(solicitacaoId);
    }

    @Transactional(readOnly = true)
    public Optional<HistoricoSolicitacao> buscarUltimo(Long solicitacaoId) {
        if (solicitacaoId == null) {
            throw new IllegalArgumentException("ID da solicitação é obrigatório");
        }
        return historicoSolicitacaoRepository.findUltimoPorSolicitacaoId(solicitacaoId);
    }
}

