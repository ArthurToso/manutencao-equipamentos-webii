package br.ufpr.tads.manutencao.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.tads.manutencao.dto.SolicitacaoRequest;
import br.ufpr.tads.manutencao.dto.SolicitacaoResponse;
import br.ufpr.tads.manutencao.mapper.SolicitacaoMapper;
import br.ufpr.tads.manutencao.model.Categoria;
import br.ufpr.tads.manutencao.model.Cliente;
import br.ufpr.tads.manutencao.model.EstadoSolicitacao;
import br.ufpr.tads.manutencao.model.HistoricoSolicitacao;
import br.ufpr.tads.manutencao.model.Solicitacao;
import br.ufpr.tads.manutencao.model.Usuario;
import br.ufpr.tads.manutencao.repository.CategoriaRepository;
import br.ufpr.tads.manutencao.repository.ClienteRepository;
import br.ufpr.tads.manutencao.repository.SolicitacaoRepository;
import br.ufpr.tads.manutencao.repository.UsuarioRepository;
import br.ufpr.tads.manutencao.security.SecurityUtils;
import br.ufpr.tads.manutencao.security.UsuarioAutenticado;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final HistoricoSolicitacaoService historicoSolicitacaoService;
    private final SolicitacaoMapper solicitacaoMapper;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
                              CategoriaRepository categoriaRepository,
                              UsuarioRepository usuarioRepository,
                              ClienteRepository clienteRepository,
                              HistoricoSolicitacaoService historicoSolicitacaoService,
                              SolicitacaoMapper solicitacaoMapper) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.historicoSolicitacaoService = historicoSolicitacaoService;
        this.solicitacaoMapper = solicitacaoMapper;
    }

    @Transactional
    public Solicitacao alterarEstado(Solicitacao solicitacao, EstadoSolicitacao novoEstado,
                                     Usuario responsavel, Usuario destino, String observacao) {
        if (solicitacao == null) {
            throw new IllegalArgumentException("Solicitação é obrigatória");
        }
        if (novoEstado == null) {
            throw new IllegalArgumentException("Novo estado é obrigatório");
        }

        EstadoSolicitacao anterior = solicitacao.getEstado();
        FluxoSolicitacao.validarTransicao(anterior, novoEstado);

        LocalDateTime agora = LocalDateTime.now();
        solicitacao.setEstado(novoEstado);
        if (solicitacao.getDataHora() == null) {
            solicitacao.setDataHora(agora);
        }   

        if (novoEstado == EstadoSolicitacao.REJEITADA) {
            solicitacao.setMotivoRejeicao(observacao);
        }

        Solicitacao salva = solicitacaoRepository.save(solicitacao);
        historicoSolicitacaoService.registrar(salva, anterior, novoEstado, responsavel, destino, observacao, agora);

        return salva;
    }

    @Transactional
    public SolicitacaoResponse criarSolicitacao(SolicitacaoRequest request, UsuarioAutenticado usuarioLogado) {
        final UsuarioAutenticado usuario = usuarioLogado != null ? usuarioLogado : SecurityUtils.getUsuarioAutenticadoObrigatorio();

        if (usuario.getClienteId() == null) {
            throw new AccessDeniedException("Apenas clientes podem criar solicitações");
        }

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setDescricaoEquipamento(request.getDescricaoEquipamento());
        solicitacao.setCategoria(categoria);
        solicitacao.setDescricaoDefeito(request.getDescricaoDefeito());
        solicitacao.setClienteId(usuario.getClienteId());

        Usuario responsavel = usuarioRepository.findById(usuario.getId())
                .orElseGet(() -> usuarioRepository.getReferenceById(usuario.getId()));

        Solicitacao salva = alterarEstado(solicitacao, EstadoSolicitacao.ABERTA, responsavel, null, null);
        return solicitacaoMapper.toResponse(salva);
    }

    public SolicitacaoResponse criarSolicitacao(SolicitacaoRequest request) {
        return criarSolicitacao(request, null);
    }

    @Transactional(readOnly = true)
    public SolicitacaoResponse buscarOrcamento(Long id, UsuarioAutenticado usuarioLogado) {
        final UsuarioAutenticado usuario = usuarioLogado != null ? usuarioLogado : SecurityUtils.getUsuarioAutenticadoObrigatorio();
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        validarPropriedadeCliente(solicitacao, usuario);
        return solicitacaoMapper.toResponse(solicitacao);
    }

    public SolicitacaoResponse buscarOrcamento(Long id) {
        return buscarOrcamento(id, null);
    }

    @Transactional(readOnly = true)
    public SolicitacaoResponse buscarPorId(Long id, UsuarioAutenticado usuarioLogado) {
        final UsuarioAutenticado usuario = usuarioLogado != null ? usuarioLogado : SecurityUtils.getUsuarioAutenticadoObrigatorio();
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        validarAcesso(solicitacao, usuario);

        Cliente cliente = null;
        if (solicitacao.getClienteId() != null) {
            cliente = clienteRepository.findById(solicitacao.getClienteId()).orElse(null);
        }

        List<HistoricoSolicitacao> historico = historicoSolicitacaoService.listar(solicitacao.getId());
        return solicitacaoMapper.toResponseComHistorico(solicitacao, historico, cliente);
    }

    public SolicitacaoResponse buscarPorId(Long id) {
        return buscarPorId(id, null);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> buscarAbertas(UsuarioAutenticado usuarioLogado) {
        final UsuarioAutenticado usuario = usuarioLogado != null ? usuarioLogado : SecurityUtils.getUsuarioAutenticadoObrigatorio();
        if (!"FUNCIONARIO".equals(usuario.getPerfil())) {
            throw new AccessDeniedException("Apenas funcionários podem listar solicitações abertas");
        }

        return solicitacaoRepository.findByEstadoOrderByDataHoraAsc(EstadoSolicitacao.ABERTA)
                .stream()
                .map(s -> {
                    Cliente cliente = null;
                    if (s.getClienteId() != null) {
                        cliente = clienteRepository.findById(s.getClienteId()).orElse(null);
                    }
                    return solicitacaoMapper.toResponse(s, cliente);
                })
                .collect(Collectors.toList());
    }

    public List<SolicitacaoResponse> buscarAbertas() {
        return buscarAbertas(null);
    }

    @Transactional
    public SolicitacaoResponse efetuarOrcamento(Long id, BigDecimal valor, UsuarioAutenticado usuarioLogado) {
        final UsuarioAutenticado usuario = usuarioLogado != null ? usuarioLogado : SecurityUtils.getUsuarioAutenticadoObrigatorio();
        if (!"FUNCIONARIO".equals(usuario.getPerfil())) {
            throw new AccessDeniedException("Apenas funcionários podem efetuar orçamento");
        }

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do orçamento deve ser maior que zero");
        }

        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        if (solicitacao.getEstado() != EstadoSolicitacao.ABERTA) {
            throw new IllegalStateException("Apenas solicitações com estado ABERTA podem receber orçamento");
        }

        solicitacao.setValorOrcamento(valor);

        Usuario responsavel = usuarioRepository.findById(usuario.getId())
                .orElseGet(() -> usuarioRepository.getReferenceById(usuario.getId()));

        Solicitacao salva = alterarEstado(solicitacao, EstadoSolicitacao.ORCADA, responsavel, null, null);

        Cliente cliente = null;
        if (salva.getClienteId() != null) {
            cliente = clienteRepository.findById(salva.getClienteId()).orElse(null);
        }

        return solicitacaoMapper.toResponse(salva, cliente);
    }

    public SolicitacaoResponse efetuarOrcamento(Long id, BigDecimal valor) {
        return efetuarOrcamento(id, valor, null);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> buscarMinhas(UsuarioAutenticado usuarioLogado) {
        final UsuarioAutenticado usuario = usuarioLogado != null ? usuarioLogado : SecurityUtils.getUsuarioAutenticadoObrigatorio();

        if (usuario.getClienteId() == null) {
            throw new AccessDeniedException("Apenas clientes possuem solicitações próprias");
        }

        return solicitacaoRepository.findByClienteIdOrderByDataHoraAsc(usuario.getClienteId())
                .stream()
                .map(solicitacaoMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<SolicitacaoResponse> buscarMinhas() {
        return buscarMinhas(null);
    }

    @Transactional
    public SolicitacaoResponse aprovarServico(Long id, UsuarioAutenticado usuarioLogado) {
        final UsuarioAutenticado usuario = usuarioLogado != null ? usuarioLogado : SecurityUtils.getUsuarioAutenticadoObrigatorio();
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        validarPropriedadeCliente(solicitacao, usuario);

        Usuario responsavel = usuarioRepository.findById(usuario.getId())
                .orElseGet(() -> usuarioRepository.getReferenceById(usuario.getId()));

        Solicitacao salva = alterarEstado(solicitacao, EstadoSolicitacao.APROVADA, responsavel, null, null);
        return solicitacaoMapper.toResponse(salva);
    }

    public SolicitacaoResponse aprovarServico(Long id) {
        return aprovarServico(id, null);
    }

    @Transactional
    public SolicitacaoResponse rejeitarServico(Long id, String motivo, UsuarioAutenticado usuarioLogado) {
        final UsuarioAutenticado usuario = usuarioLogado != null ? usuarioLogado : SecurityUtils.getUsuarioAutenticadoObrigatorio();
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        validarPropriedadeCliente(solicitacao, usuario);

        Usuario responsavel = usuarioRepository.findById(usuario.getId())
                .orElseGet(() -> usuarioRepository.getReferenceById(usuario.getId()));

        Solicitacao salva = alterarEstado(solicitacao, EstadoSolicitacao.REJEITADA, responsavel, null, motivo);
        return solicitacaoMapper.toResponse(salva);
    }

    public SolicitacaoResponse rejeitarServico(Long id, String motivo) {
        return rejeitarServico(id, motivo, null);
    }

    @Transactional
    public SolicitacaoResponse resgatarServico(Long id, UsuarioAutenticado usuarioLogado) {
        final UsuarioAutenticado usuario = usuarioLogado != null ? usuarioLogado : SecurityUtils.getUsuarioAutenticadoObrigatorio();
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        validarPropriedadeCliente(solicitacao, usuario);

        Usuario responsavel = usuarioRepository.findById(usuario.getId())
                .orElseGet(() -> usuarioRepository.getReferenceById(usuario.getId()));

        Solicitacao salva = alterarEstado(solicitacao, EstadoSolicitacao.APROVADA, responsavel, null, null);
        return solicitacaoMapper.toResponse(salva);
    }

    public SolicitacaoResponse resgatarServico(Long id) {
        return resgatarServico(id, null);
    }

    @Transactional
    public SolicitacaoResponse pagarServico(Long id, UsuarioAutenticado usuarioLogado) {
        final UsuarioAutenticado usuario = usuarioLogado != null ? usuarioLogado : SecurityUtils.getUsuarioAutenticadoObrigatorio();
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        validarPropriedadeCliente(solicitacao, usuario);

        Usuario responsavel = usuarioRepository.findById(usuario.getId())
                .orElseGet(() -> usuarioRepository.getReferenceById(usuario.getId()));

        Solicitacao salva = alterarEstado(solicitacao, EstadoSolicitacao.PAGA, responsavel, null, null);
        return solicitacaoMapper.toResponse(salva);
    }

    public SolicitacaoResponse pagarServico(Long id) {
        return pagarServico(id, null);
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
}
