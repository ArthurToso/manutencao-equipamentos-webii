package br.ufpr.tads.manutencao.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import br.ufpr.tads.manutencao.dto.SolicitacaoRequest;
import br.ufpr.tads.manutencao.dto.SolicitacaoResponse;
import br.ufpr.tads.manutencao.model.Categoria;
import br.ufpr.tads.manutencao.model.EstadoSolicitacao;
import br.ufpr.tads.manutencao.model.Solicitacao;
import br.ufpr.tads.manutencao.repository.CategoriaRepository;
import br.ufpr.tads.manutencao.repository.SolicitacaoRepository;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final CategoriaRepository categoriaRepository;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
                              CategoriaRepository categoriaRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public SolicitacaoResponse criarSolicitacao(SolicitacaoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setDescricaoEquipamento(request.getDescricaoEquipamento());
        solicitacao.setCategoria(categoria);
        solicitacao.setDescricaoDefeito(request.getDescricaoDefeito());
        solicitacao.setDataHora(LocalDateTime.now());
        solicitacao.setEstado(EstadoSolicitacao.ABERTA);
        solicitacao.setClienteId(request.getClienteId());

        Solicitacao salva = solicitacaoRepository.save(solicitacao);

        return toResponse(salva);
    }

    private SolicitacaoResponse toResponse(Solicitacao solicitacao) {
        SolicitacaoResponse response = new SolicitacaoResponse();
        response.setId(solicitacao.getId());
        response.setDescricaoEquipamento(solicitacao.getDescricaoEquipamento());
        response.setCategoriaId(solicitacao.getCategoria().getId());
        response.setCategoriaNome(solicitacao.getCategoria().getNome());
        response.setDescricaoDefeito(solicitacao.getDescricaoDefeito());
        response.setDataHora(solicitacao.getDataHora());
        response.setEstado(solicitacao.getEstado().name());
        response.setClienteId(solicitacao.getClienteId());
        return response;
    }
}
