package br.ufpr.tads.manutencao.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.tads.manutencao.dto.SolicitacaoRequest;
import br.ufpr.tads.manutencao.dto.SolicitacaoResponse;
import br.ufpr.tads.manutencao.exception.RecursoNaoEncontradoException;
import br.ufpr.tads.manutencao.mapper.SolicitacaoMapper;
import br.ufpr.tads.manutencao.model.Categoria;
import br.ufpr.tads.manutencao.model.Cliente;
import br.ufpr.tads.manutencao.model.EstadoSolicitacao;
import br.ufpr.tads.manutencao.model.Funcionario;
import br.ufpr.tads.manutencao.model.HistoricoSolicitacao;
import br.ufpr.tads.manutencao.model.Perfil;
import br.ufpr.tads.manutencao.model.Solicitacao;
import br.ufpr.tads.manutencao.model.Usuario;
import br.ufpr.tads.manutencao.repository.CategoriaRepository;
import br.ufpr.tads.manutencao.repository.ClienteRepository;
import br.ufpr.tads.manutencao.repository.FuncionarioRepository;
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
    private final FuncionarioRepository funcionarioRepository;
    private final HistoricoSolicitacaoService historicoSolicitacaoService;
    private final SolicitacaoMapper solicitacaoMapper;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
                              CategoriaRepository categoriaRepository,
                              UsuarioRepository usuarioRepository,
                              ClienteRepository clienteRepository,
                              HistoricoSolicitacaoService historicoSolicitacaoService,
                              SolicitacaoMapper solicitacaoMapper) {
        this(solicitacaoRepository, categoriaRepository, usuarioRepository,
                clienteRepository, null, historicoSolicitacaoService, solicitacaoMapper);
    }

    @Autowired
    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
                              CategoriaRepository categoriaRepository,
                              UsuarioRepository usuarioRepository,
                              ClienteRepository clienteRepository,
                              FuncionarioRepository funcionarioRepository,
                              HistoricoSolicitacaoService historicoSolicitacaoService,
                              SolicitacaoMapper solicitacaoMapper) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.funcionarioRepository = funcionarioRepository;
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
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação não encontrada"));
        validarPropriedadeCliente(solicitacao, usuario);

        if (solicitacao.getEstado() == EstadoSolicitacao.ABERTA || solicitacao.getValorOrcamento() == null) {
            throw new IllegalStateException("A solicitação ainda não foi orçada");
        }

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

    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> listarParaFuncionario(String filtro, LocalDate dataInicial, LocalDate dataFinal, UsuarioAutenticado usuarioLogado) {
        final UsuarioAutenticado usuario = usuarioLogado != null ? usuarioLogado : SecurityUtils.getUsuarioAutenticadoObrigatorio();
        if (!"FUNCIONARIO".equals(usuario.getPerfil())) {
            throw new AccessDeniedException("Apenas funcionários podem visualizar solicitações");
        }

        String filtroNorm = (filtro == null || filtro.isBlank()) ? "TODAS" : filtro.trim().toUpperCase();

        if ("HOJE".equals(filtroNorm)) {
            LocalDate hoje = LocalDate.now();
            LocalDateTime inicio = hoje.atStartOfDay();
            LocalDateTime fim = hoje.plusDays(1).atStartOfDay();
            return solicitacaoRepository.listarPorPeriodoParaFuncionario(usuario.getId(), inicio, fim);
        } else if ("PERIODO".equals(filtroNorm)) {
            if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
                throw new IllegalArgumentException("A data inicial não pode ser posterior à data final");
            }
            LocalDateTime inicio = dataInicial != null ? dataInicial.atStartOfDay() : null;
            LocalDateTime fim = dataFinal != null ? dataFinal.plusDays(1).atStartOfDay() : null;

            if (inicio != null && fim != null) {
                return solicitacaoRepository.listarPorPeriodoParaFuncionario(usuario.getId(), inicio, fim);
            } else if (inicio != null) {
                return solicitacaoRepository.listarAposDataParaFuncionario(usuario.getId(), inicio);
            } else if (fim != null) {
                return solicitacaoRepository.listarAteDataParaFuncionario(usuario.getId(), fim);
            } else {
                return solicitacaoRepository.listarTodasParaFuncionario(usuario.getId());
            }
        } else if ("TODAS".equals(filtroNorm)) {
            return solicitacaoRepository.listarTodasParaFuncionario(usuario.getId());
        } else {
            throw new IllegalArgumentException("Filtro inválido. Use HOJE, PERIODO ou TODAS");
        }
    }

    public List<SolicitacaoResponse> listarParaFuncionario(String filtro, LocalDate dataInicial, LocalDate dataFinal) {
        return listarParaFuncionario(filtro, dataInicial, dataFinal, null);
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
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação não encontrada"));
        validarPropriedadeCliente(solicitacao, usuario);

        if (solicitacao.getEstado() != EstadoSolicitacao.ORCADA
                || solicitacao.getValorOrcamento() == null
                || solicitacao.getValorOrcamento().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Só é possível aprovar uma solicitação orçada");
        }

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

    @Transactional
    public SolicitacaoResponse finalizarSolicitacao(Long id, UsuarioAutenticado usuarioLogado) {
        final UsuarioAutenticado usuario = usuarioLogado != null
                ? usuarioLogado
                : SecurityUtils.getUsuarioAutenticadoObrigatorio();

        if (!"FUNCIONARIO".equals(usuario.getPerfil())) {
            throw new AccessDeniedException("Apenas funcionários podem finalizar solicitações");
        }

        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        if (solicitacao.getEstado() != EstadoSolicitacao.PAGA) {
            throw new IllegalStateException("Apenas solicitações pagas podem ser finalizadas");
        }

        Usuario responsavel = usuarioRepository.findById(usuario.getId())
                .orElseGet(() -> usuarioRepository.getReferenceById(usuario.getId()));

        Solicitacao salva = alterarEstado(
                solicitacao, EstadoSolicitacao.FINALIZADA, responsavel, null, null);
        return solicitacaoMapper.toResponse(salva);
    }

    public SolicitacaoResponse finalizarSolicitacao(Long id) {
        return finalizarSolicitacao(id, null);
    }

    @Transactional
    public SolicitacaoResponse redirecionarManutencao(Long id, Long funcionarioDestinoId, UsuarioAutenticado usuarioLogado) {
        final UsuarioAutenticado usuario = usuarioLogado != null
                ? usuarioLogado
                : SecurityUtils.getUsuarioAutenticadoObrigatorio();

        if (!"FUNCIONARIO".equals(usuario.getPerfil())) {
            throw new AccessDeniedException("Apenas funcionários podem redirecionar solicitações");
        }

        if (funcionarioDestinoId == null) {
            throw new IllegalArgumentException("Funcionário de destino é obrigatório");
        }

        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação não encontrada"));

        if (solicitacao.getEstado() != EstadoSolicitacao.APROVADA
                && solicitacao.getEstado() != EstadoSolicitacao.REDIRECIONADA) {
            throw new IllegalStateException("Apenas solicitações aprovadas ou redirecionadas podem ser redirecionadas");
        }

        Usuario destino = null;
        if (funcionarioRepository != null) {
            Optional<Funcionario> funcOpt = funcionarioRepository.findById(funcionarioDestinoId);
            if (funcOpt.isPresent()) {
                Funcionario funcDestino = funcOpt.get();
                if (usuario.getFuncionarioId() != null && usuario.getFuncionarioId().equals(funcDestino.getId())) {
                    throw new IllegalArgumentException("Funcionário destino não pode ser o mesmo funcionário responsável");
                }
                destino = funcDestino.getUsuario();
            }
        }

        if (destino == null) {
            destino = usuarioRepository.findById(funcionarioDestinoId)
                    .orElseThrow(() -> new IllegalArgumentException("Funcionário de destino não encontrado"));
        }

        if (destino.getId() != null && destino.getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Funcionário destino não pode ser o mesmo funcionário responsável");
        }

        if (destino.getPerfil() != Perfil.FUNCIONARIO) {
            throw new IllegalArgumentException("Funcionário destino deve ser do perfil FUNCIONARIO");
        }

        if (!destino.isAtivo()) {
            throw new IllegalStateException("Funcionário destino deve estar ativo");
        }

        Usuario responsavel = usuarioRepository.findById(usuario.getId())
                .orElseGet(() -> usuarioRepository.getReferenceById(usuario.getId()));

        Solicitacao salva = alterarEstado(solicitacao, EstadoSolicitacao.REDIRECIONADA, responsavel, destino, null);

        Cliente cliente = null;
        if (salva.getClienteId() != null) {
            cliente = clienteRepository.findById(salva.getClienteId()).orElse(null);
        }

        List<HistoricoSolicitacao> historico = historicoSolicitacaoService.listar(salva.getId());
        return solicitacaoMapper.toResponseComHistorico(salva, historico, cliente);
    }

    public SolicitacaoResponse redirecionarManutencao(Long id, Long funcionarioDestinoId) {
        return redirecionarManutencao(id, funcionarioDestinoId, null);
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
