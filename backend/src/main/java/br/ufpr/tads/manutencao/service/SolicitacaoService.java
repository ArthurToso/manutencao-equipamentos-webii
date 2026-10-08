package br.ufpr.tads.manutencao.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import br.ufpr.tads.manutencao.dto.SolicitacaoRequest;
import br.ufpr.tads.manutencao.dto.SolicitacaoResponse;
import br.ufpr.tads.manutencao.model.Categoria;
import br.ufpr.tads.manutencao.model.EstadoSolicitacao;
import br.ufpr.tads.manutencao.model.Solicitacao;
import br.ufpr.tads.manutencao.dto.HistoricoDTO;
import br.ufpr.tads.manutencao.model.HistoricoSolicitacao;
import br.ufpr.tads.manutencao.repository.HistoricoSolicitacaoRepository;
import br.ufpr.tads.manutencao.repository.CategoriaRepository;
import br.ufpr.tads.manutencao.repository.SolicitacaoRepository;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final HistoricoSolicitacaoRepository historicoSolicitacaoRepository;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
                              CategoriaRepository categoriaRepository,
                              HistoricoSolicitacaoRepository historicoSolicitacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.categoriaRepository = categoriaRepository;
        this.historicoSolicitacaoRepository = historicoSolicitacaoRepository;
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
        salvarHistorico(salva);

        return toResponse(salva);
    }

    public SolicitacaoResponse buscarOrcamento(Long id) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        return toResponse(solicitacao);
    }

    public SolicitacaoResponse buscarPorId(Long id) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        return toResponseComHistorico(solicitacao);
    }

    public java.util.List<SolicitacaoResponse> buscarPorClienteId(Long clienteId) {
        return solicitacaoRepository.findByClienteIdOrderByDataHoraDesc(clienteId)
                .stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    public SolicitacaoResponse aprovarServico(Long id) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        
        if (solicitacao.getEstado() != EstadoSolicitacao.ORCADA) {
            throw new IllegalStateException("A solicitação não está no estado ORCADA");
        }
        
        solicitacao.setEstado(EstadoSolicitacao.APROVADA);
        Solicitacao salva = solicitacaoRepository.save(solicitacao);
        salvarHistorico(salva);
        return toResponse(salva);
    }

    public SolicitacaoResponse rejeitarServico(Long id, String motivo) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        
        if (solicitacao.getEstado() != EstadoSolicitacao.ORCADA) {
            throw new IllegalStateException("A solicitação não está no estado ORCADA");
        }
        
        solicitacao.setEstado(EstadoSolicitacao.REJEITADA);
        solicitacao.setMotivoRejeicao(motivo);
        Solicitacao salva = solicitacaoRepository.save(solicitacao);
        salvarHistorico(salva);
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
        response.setValorOrcamento(solicitacao.getValorOrcamento());
        response.setMotivoRejeicao(solicitacao.getMotivoRejeicao());
        return response;
    }

    private SolicitacaoResponse toResponseComHistorico(Solicitacao solicitacao) {
        SolicitacaoResponse response = toResponse(solicitacao);
        java.util.List<HistoricoSolicitacao> historicos = historicoSolicitacaoRepository.findBySolicitacaoIdOrderByDataHoraAsc(solicitacao.getId());
        java.util.List<HistoricoDTO> historicoDTOs = historicos.stream().map(h -> {
            HistoricoDTO dto = new HistoricoDTO();
            dto.setEstado(h.getEstado().name());
            dto.setDataHora(h.getDataHora());
            if (h.getUsuario() != null) {
                dto.setNomeFuncionario(h.getUsuario().getNome());
            }
            return dto;
        }).collect(java.util.stream.Collectors.toList());
        response.setHistorico(historicoDTOs);
        return response;
    }

    private void salvarHistorico(Solicitacao solicitacao) {
        HistoricoSolicitacao historico = new HistoricoSolicitacao();
        historico.setSolicitacao(solicitacao);
        historico.setEstado(solicitacao.getEstado());
        historico.setDataHora(LocalDateTime.now());
        historicoSolicitacaoRepository.save(historico);
    }
}
