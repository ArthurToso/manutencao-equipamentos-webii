package br.ufpr.tads.manutencao.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import br.ufpr.tads.manutencao.dto.HistoricoDTO;
import br.ufpr.tads.manutencao.dto.SolicitacaoRequest;
import br.ufpr.tads.manutencao.dto.SolicitacaoResponse;
import br.ufpr.tads.manutencao.model.Categoria;
import br.ufpr.tads.manutencao.model.EstadoSolicitacao;
import br.ufpr.tads.manutencao.model.HistoricoSolicitacao;
import br.ufpr.tads.manutencao.model.Solicitacao;
import br.ufpr.tads.manutencao.model.Usuario;
import br.ufpr.tads.manutencao.repository.CategoriaRepository;
import br.ufpr.tads.manutencao.repository.HistoricoSolicitacaoRepository;
import br.ufpr.tads.manutencao.repository.SolicitacaoRepository;
import br.ufpr.tads.manutencao.repository.UsuarioRepository;
import br.ufpr.tads.manutencao.security.SecurityUtils;
import br.ufpr.tads.manutencao.security.UsuarioAutenticado;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final HistoricoSolicitacaoRepository historicoSolicitacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
                              CategoriaRepository categoriaRepository,
                              HistoricoSolicitacaoRepository historicoSolicitacaoRepository,
                              UsuarioRepository usuarioRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.categoriaRepository = categoriaRepository;
        this.historicoSolicitacaoRepository = historicoSolicitacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public SolicitacaoResponse criarSolicitacao(SolicitacaoRequest request) {
        UsuarioAutenticado usuarioLogado = SecurityUtils.getUsuarioAutenticadoObrigatorio();

        if (usuarioLogado.getClienteId() == null) {
            throw new AccessDeniedException("Apenas clientes podem criar solicitações");
        }

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setDescricaoEquipamento(request.getDescricaoEquipamento());
        solicitacao.setCategoria(categoria);
        solicitacao.setDescricaoDefeito(request.getDescricaoDefeito());
        solicitacao.setDataHora(LocalDateTime.now());
        solicitacao.setEstado(EstadoSolicitacao.ABERTA);
        solicitacao.setClienteId(usuarioLogado.getClienteId());

        Solicitacao salva = solicitacaoRepository.save(solicitacao);

        Usuario responsavel = usuarioRepository.getReferenceById(usuarioLogado.getId());
        salvarHistorico(salva, responsavel);

        return toResponse(salva);
    }

    public SolicitacaoResponse buscarOrcamento(Long id) {
        UsuarioAutenticado usuarioLogado = SecurityUtils.getUsuarioAutenticadoObrigatorio();
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        validarPropriedadeCliente(solicitacao, usuarioLogado);
        return toResponse(solicitacao);
    }

    public SolicitacaoResponse buscarPorId(Long id) {
        UsuarioAutenticado usuarioLogado = SecurityUtils.getUsuarioAutenticadoObrigatorio();
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        validarAcesso(solicitacao, usuarioLogado);
        return toResponseComHistorico(solicitacao);
    }

    public List<SolicitacaoResponse> buscarMinhas() {
        UsuarioAutenticado usuarioLogado = SecurityUtils.getUsuarioAutenticadoObrigatorio();

        if (usuarioLogado.getClienteId() == null) {
            throw new AccessDeniedException("Apenas clientes possuem solicitações próprias");
        }

        return solicitacaoRepository.findByClienteIdOrderByDataHoraDesc(usuarioLogado.getClienteId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SolicitacaoResponse aprovarServico(Long id) {
        UsuarioAutenticado usuarioLogado = SecurityUtils.getUsuarioAutenticadoObrigatorio();
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        validarPropriedadeCliente(solicitacao, usuarioLogado);

        if (solicitacao.getEstado() != EstadoSolicitacao.ORCADA) {
            throw new IllegalStateException("A solicitação não está no estado ORCADA");
        }

        solicitacao.setEstado(EstadoSolicitacao.APROVADA);
        Solicitacao salva = solicitacaoRepository.save(solicitacao);

        Usuario responsavel = usuarioRepository.getReferenceById(usuarioLogado.getId());
        salvarHistorico(salva, responsavel);

        return toResponse(salva);
    }

    public SolicitacaoResponse rejeitarServico(Long id, String motivo) {
        UsuarioAutenticado usuarioLogado = SecurityUtils.getUsuarioAutenticadoObrigatorio();
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        validarPropriedadeCliente(solicitacao, usuarioLogado);

        if (solicitacao.getEstado() != EstadoSolicitacao.ORCADA) {
            throw new IllegalStateException("A solicitação não está no estado ORCADA");
        }

        solicitacao.setEstado(EstadoSolicitacao.REJEITADA);
        solicitacao.setMotivoRejeicao(motivo);
        Solicitacao salva = solicitacaoRepository.save(solicitacao);

        Usuario responsavel = usuarioRepository.getReferenceById(usuarioLogado.getId());
        salvarHistorico(salva, responsavel);

        return toResponse(salva);
    }

    private void validarPropriedadeCliente(Solicitacao solicitacao, UsuarioAutenticado usuario) {
        if (usuario.getClienteId() == null || !usuario.getClienteId().equals(solicitacao.getClienteId())) {
            throw new AccessDeniedException("Você não tem permissão para acessar esta solicitação");
        }
    }

    private void validarAcesso(Solicitacao solicitacao, UsuarioAutenticado usuario) {
        if ("FUNCIONARIO".equals(usuario.getPerfil())) {
            return;
        }
        validarPropriedadeCliente(solicitacao, usuario);
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
        List<HistoricoSolicitacao> historicos = historicoSolicitacaoRepository.findBySolicitacaoIdOrderByDataHoraAsc(solicitacao.getId());
        List<HistoricoDTO> historicoDTOs = historicos.stream().map(h -> {
            HistoricoDTO dto = new HistoricoDTO();
            dto.setEstado(h.getEstado().name());
            dto.setDataHora(h.getDataHora());
            if (h.getUsuario() != null) {
                dto.setNomeFuncionario(h.getUsuario().getNome());
            }
            return dto;
        }).collect(Collectors.toList());
        response.setHistorico(historicoDTOs);
        return response;
    }

    private void salvarHistorico(Solicitacao solicitacao, Usuario usuario) {
        HistoricoSolicitacao historico = new HistoricoSolicitacao();
        historico.setSolicitacao(solicitacao);
        historico.setEstado(solicitacao.getEstado());
        historico.setDataHora(LocalDateTime.now());
        historico.setUsuario(usuario);
        historicoSolicitacaoRepository.save(historico);
    }
}
