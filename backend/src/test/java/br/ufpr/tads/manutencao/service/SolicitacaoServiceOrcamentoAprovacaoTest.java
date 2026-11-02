package br.ufpr.tads.manutencao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import br.ufpr.tads.manutencao.dto.SolicitacaoResponse;
import br.ufpr.tads.manutencao.exception.RecursoNaoEncontradoException;
import br.ufpr.tads.manutencao.mapper.SolicitacaoMapper;
import br.ufpr.tads.manutencao.model.Categoria;
import br.ufpr.tads.manutencao.model.EstadoSolicitacao;
import br.ufpr.tads.manutencao.model.Perfil;
import br.ufpr.tads.manutencao.model.Solicitacao;
import br.ufpr.tads.manutencao.model.Usuario;
import br.ufpr.tads.manutencao.repository.CategoriaRepository;
import br.ufpr.tads.manutencao.repository.ClienteRepository;
import br.ufpr.tads.manutencao.repository.SolicitacaoRepository;
import br.ufpr.tads.manutencao.repository.UsuarioRepository;
import br.ufpr.tads.manutencao.security.UsuarioAutenticado;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceOrcamentoAprovacaoTest {

    @Mock
    private SolicitacaoRepository solicitacaoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private HistoricoSolicitacaoService historicoSolicitacaoService;

    @Spy
    private SolicitacaoMapper solicitacaoMapper = new SolicitacaoMapper();

    @InjectMocks
    private SolicitacaoService solicitacaoService;

    private Usuario usuarioCliente;
    private UsuarioAutenticado authCliente;
    private UsuarioAutenticado authOutroCliente;
    private Categoria categoriaTeclado;
    private Solicitacao solicitacaoOrcada;

    @BeforeEach
    void setUp() {
        usuarioCliente = new Usuario("José", "jose@example.com", "hash", "salt", Perfil.CLIENTE);
        usuarioCliente.setId(4L);

        authCliente = new UsuarioAutenticado(4L, "jose@example.com", "CLIENTE", 2L, null);
        authOutroCliente = new UsuarioAutenticado(5L, "joana@example.com", "CLIENTE", 3L, null);

        categoriaTeclado = new Categoria("Teclado");
        categoriaTeclado.setId(5L);

        solicitacaoOrcada = new Solicitacao();
        solicitacaoOrcada.setId(14L);
        solicitacaoOrcada.setClienteId(2L);
        solicitacaoOrcada.setCategoria(categoriaTeclado);
        solicitacaoOrcada.setDescricaoEquipamento("Notebook/Teclado Microsoft Wired 600");
        solicitacaoOrcada.setDescricaoDefeito("Líquido derramado, teclas grudando.");
        solicitacaoOrcada.setEstado(EstadoSolicitacao.ORCADA);
        solicitacaoOrcada.setValorOrcamento(new BigDecimal("70.00"));
        solicitacaoOrcada.setDataHora(LocalDateTime.now().minusDays(2));
    }

    @Test
    @DisplayName("RF006: aprovar a partir de ORCADA muda estado para APROVADA e grava uma linha de histórico")
    void aprovarPartirDeOrcada_estadoViraAprovadaEGravaUmaLinhaComUsuarioDoCliente() {
        when(solicitacaoRepository.findById(14L)).thenReturn(Optional.of(solicitacaoOrcada));
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(usuarioCliente));
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitacaoResponse response = solicitacaoService.aprovarServico(14L, authCliente);

        assertNotNull(response);
        assertEquals("APROVADA", response.getEstado());
        assertEquals(new BigDecimal("70.00"), response.getValorOrcamento());
        assertEquals(EstadoSolicitacao.APROVADA, solicitacaoOrcada.getEstado());

        verify(historicoSolicitacaoService).registrar(
                eq(solicitacaoOrcada),
                eq(EstadoSolicitacao.ORCADA),
                eq(EstadoSolicitacao.APROVADA),
                eq(usuarioCliente),
                eq(null),
                eq(null),
                any(LocalDateTime.class)
        );
    }

    @ParameterizedTest
    @EnumSource(value = EstadoSolicitacao.class, names = {"ABERTA", "APROVADA", "REJEITADA", "FINALIZADA"})
    @DisplayName("RF006: aprovar em ABERTA, APROVADA, REJEITADA ou FINALIZADA retorna 409, estado intacto e nenhuma linha gravada")
    void aprovarEstadosNaoPermitidos_lanca409EstadoIntactoENenhumaLinhaGravada(EstadoSolicitacao estadoInvalido) {
        solicitacaoOrcada.setEstado(estadoInvalido);
        when(solicitacaoRepository.findById(14L)).thenReturn(Optional.of(solicitacaoOrcada));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                solicitacaoService.aprovarServico(14L, authCliente));

        assertEquals("Só é possível aprovar uma solicitação orçada", ex.getMessage());
        assertEquals(estadoInvalido, solicitacaoOrcada.getEstado());

        verify(solicitacaoRepository, never()).save(any(Solicitacao.class));
        verify(historicoSolicitacaoService, never()).registrar(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("RF006: aprovar com valorOrcamento nulo retorna 409")
    void aprovarComValorOrcamentoNulo_lanca409() {
        solicitacaoOrcada.setValorOrcamento(null);
        when(solicitacaoRepository.findById(14L)).thenReturn(Optional.of(solicitacaoOrcada));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                solicitacaoService.aprovarServico(14L, authCliente));

        assertEquals("Só é possível aprovar uma solicitação orçada", ex.getMessage());
        verify(solicitacaoRepository, never()).save(any());
        verify(historicoSolicitacaoService, never()).registrar(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("RF006: aprovar solicitação inexistente retorna 404 RecursoNaoEncontradoException")
    void aprovarSolicitacaoInexistente_lanca404RecursoNaoEncontrado() {
        when(solicitacaoRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () ->
                solicitacaoService.aprovarServico(9999L, authCliente));

        verify(historicoSolicitacaoService, never()).registrar(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("RF006: aprovar de outro cliente é negado com AccessDeniedException")
    void aprovarDeOutroCliente_lancaAccessDeniedException() {
        when(solicitacaoRepository.findById(14L)).thenReturn(Optional.of(solicitacaoOrcada));

        assertThrows(AccessDeniedException.class, () ->
                solicitacaoService.aprovarServico(14L, authOutroCliente));

        verify(historicoSolicitacaoService, never()).registrar(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("RF005: buscarOrcamento em estado ABERTA retorna 409")
    void buscarOrcamento_emAberta_lanca409() {
        solicitacaoOrcada.setEstado(EstadoSolicitacao.ABERTA);
        solicitacaoOrcada.setValorOrcamento(null);
        when(solicitacaoRepository.findById(14L)).thenReturn(Optional.of(solicitacaoOrcada));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                solicitacaoService.buscarOrcamento(14L, authCliente));

        assertEquals("A solicitação ainda não foi orçada", ex.getMessage());
    }

    @Test
    @DisplayName("RF005: buscarOrcamento com valorOrcamento nulo retorna 409")
    void buscarOrcamento_comValorOrcamentoNulo_lanca409() {
        solicitacaoOrcada.setValorOrcamento(null);
        when(solicitacaoRepository.findById(14L)).thenReturn(Optional.of(solicitacaoOrcada));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                solicitacaoService.buscarOrcamento(14L, authCliente));

        assertEquals("A solicitação ainda não foi orçada", ex.getMessage());
    }

    @Test
    @DisplayName("RF005: buscarOrcamento em ORCADA retorna dados com valor")
    void buscarOrcamento_emOrcada_retornaDadosComValor() {
        when(solicitacaoRepository.findById(14L)).thenReturn(Optional.of(solicitacaoOrcada));

        SolicitacaoResponse response = solicitacaoService.buscarOrcamento(14L, authCliente);

        assertNotNull(response);
        assertEquals(14L, response.getId());
        assertEquals("ORCADA", response.getEstado());
        assertEquals(new BigDecimal("70.00"), response.getValorOrcamento());
        assertEquals("Teclado", response.getCategoriaNome());
        assertEquals("Notebook/Teclado Microsoft Wired 600", response.getDescricaoEquipamento());
    }

    @Test
    @DisplayName("RF005: buscarOrcamento para solicitação inexistente retorna 404 RecursoNaoEncontradoException")
    void buscarOrcamento_solicitacaoInexistente_lanca404RecursoNaoEncontrado() {
        when(solicitacaoRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () ->
                solicitacaoService.buscarOrcamento(9999L, authCliente));
    }

    @Test
    @DisplayName("RF005: buscarOrcamento de outro cliente é negado com AccessDeniedException")
    void buscarOrcamento_deOutroCliente_lancaAccessDeniedException() {
        when(solicitacaoRepository.findById(14L)).thenReturn(Optional.of(solicitacaoOrcada));

        assertThrows(AccessDeniedException.class, () ->
                solicitacaoService.buscarOrcamento(14L, authOutroCliente));
    }
}

