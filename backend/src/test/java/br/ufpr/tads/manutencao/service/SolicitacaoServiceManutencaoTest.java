package br.ufpr.tads.manutencao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import br.ufpr.tads.manutencao.dto.SolicitacaoResponse;
import br.ufpr.tads.manutencao.mapper.SolicitacaoMapper;
import br.ufpr.tads.manutencao.model.Categoria;
import br.ufpr.tads.manutencao.model.EstadoSolicitacao;
import br.ufpr.tads.manutencao.model.Perfil;
import br.ufpr.tads.manutencao.model.Solicitacao;
import br.ufpr.tads.manutencao.model.Usuario;
import br.ufpr.tads.manutencao.repository.CategoriaRepository;
import br.ufpr.tads.manutencao.repository.ClienteRepository;
import br.ufpr.tads.manutencao.repository.FuncionarioRepository;
import br.ufpr.tads.manutencao.repository.SolicitacaoRepository;
import br.ufpr.tads.manutencao.repository.UsuarioRepository;
import br.ufpr.tads.manutencao.security.UsuarioAutenticado;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceManutencaoTest {

    @Mock
    private SolicitacaoRepository solicitacaoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private HistoricoSolicitacaoService historicoSolicitacaoService;

    @Spy
    private SolicitacaoMapper solicitacaoMapper = new SolicitacaoMapper();

    @InjectMocks
    private SolicitacaoService solicitacaoService;

    private UsuarioAutenticado authFuncionario;
    private Usuario usuarioFuncionario;
    private Solicitacao solicitacao;

    @BeforeEach
    void setUp() {
        usuarioFuncionario = new Usuario("Maria", "maria@example.com", "hash", "salt", Perfil.FUNCIONARIO);
        usuarioFuncionario.setId(1L);
        authFuncionario = new UsuarioAutenticado(1L, "maria@example.com", "FUNCIONARIO", null, 1L);

        solicitacao = new Solicitacao();
        solicitacao.setId(100L);
        solicitacao.setCategoria(new Categoria("Notebook"));
        solicitacao.setDescricaoEquipamento("Notebook Dell");
        solicitacao.setDescricaoDefeito("Não liga");
        solicitacao.setDataHora(LocalDateTime.of(2026, 11, 1, 10, 0));
        solicitacao.setEstado(EstadoSolicitacao.APROVADA);
    }

    @Test
    void efetuarManutencaoAtualizaSolicitacaoERegistraFuncionarioNoHistorico() {
        when(solicitacaoRepository.findById(100L)).thenReturn(Optional.of(solicitacao));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioFuncionario));
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SolicitacaoResponse response = solicitacaoService.efetuarManutencao(
                100L, "  Substituição da bateria  ", "  Evitar manter conectada após a carga.  ", authFuncionario);

        assertNotNull(response);
        assertEquals(EstadoSolicitacao.ARRUMADA, solicitacao.getEstado());
        assertEquals("Substituição da bateria", solicitacao.getDescricaoManutencao());
        assertEquals("Evitar manter conectada após a carga.", solicitacao.getOrientacoesCliente());
        assertEquals("ARRUMADA", response.getEstado());
        assertEquals("Substituição da bateria", response.getDescricaoManutencao());
        assertEquals("Evitar manter conectada após a carga.", response.getOrientacoesCliente());
        assertEquals(LocalDateTime.of(2026, 11, 1, 10, 0), solicitacao.getDataHora());

        verify(solicitacaoRepository).save(solicitacao);
        verify(historicoSolicitacaoService).registrar(
                eq(solicitacao),
                eq(EstadoSolicitacao.APROVADA),
                eq(EstadoSolicitacao.ARRUMADA),
                eq(usuarioFuncionario),
                eq(null),
                eq(null),
                any(LocalDateTime.class));
    }

    @Test
    void efetuarManutencaoAceitaEstadoRedirecionada() {
        solicitacao.setEstado(EstadoSolicitacao.REDIRECIONADA);
        when(solicitacaoRepository.findById(100L)).thenReturn(Optional.of(solicitacao));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioFuncionario));
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        solicitacaoService.efetuarManutencao(100L, "Reparo realizado", "Uso normal.", authFuncionario);

        assertEquals(EstadoSolicitacao.ARRUMADA, solicitacao.getEstado());
        verify(historicoSolicitacaoService).registrar(
                eq(solicitacao),
                eq(EstadoSolicitacao.REDIRECIONADA),
                eq(EstadoSolicitacao.ARRUMADA),
                eq(usuarioFuncionario),
                eq(null),
                eq(null),
                any(LocalDateTime.class));
    }

    @Test
    void clienteNaoPodeEfetuarManutencao() {
        UsuarioAutenticado authCliente = new UsuarioAutenticado(3L, "joao@example.com", "CLIENTE", 10L, null);

        assertThrows(AccessDeniedException.class, () ->
                solicitacaoService.efetuarManutencao(100L, "Reparo", "Orientações", authCliente));

        verify(solicitacaoRepository, never()).save(any(Solicitacao.class));
        verify(historicoSolicitacaoService, never()).registrar(
                any(), any(), any(), any(), any(), any(), any(LocalDateTime.class));
    }

    @Test
    void estadoNaoPermitidoNaoSalvaSolicitacaoNemHistorico() {
        solicitacao.setEstado(EstadoSolicitacao.ORCADA);
        when(solicitacaoRepository.findById(100L)).thenReturn(Optional.of(solicitacao));

        assertThrows(IllegalStateException.class, () ->
                solicitacaoService.efetuarManutencao(100L, "Reparo", "Orientações", authFuncionario));

        verify(solicitacaoRepository, never()).save(any(Solicitacao.class));
        verify(historicoSolicitacaoService, never()).registrar(
                any(), any(), any(), any(), any(), any(), any(LocalDateTime.class));
    }

    @Test
    void camposDeManutencaoEmBrancoSaoRejeitados() {
        assertThrows(IllegalArgumentException.class, () ->
                solicitacaoService.efetuarManutencao(100L, " ", "Orientações", authFuncionario));
        assertThrows(IllegalArgumentException.class, () ->
                solicitacaoService.efetuarManutencao(100L, "Reparo", " ", authFuncionario));

        verify(solicitacaoRepository, never()).findById(any());
    }
}
