package br.ufpr.tads.manutencao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import br.ufpr.tads.manutencao.dto.SolicitacaoRequest;
import br.ufpr.tads.manutencao.dto.SolicitacaoResponse;
import br.ufpr.tads.manutencao.mapper.SolicitacaoMapper;
import br.ufpr.tads.manutencao.model.Categoria;
import br.ufpr.tads.manutencao.model.EstadoSolicitacao;
import br.ufpr.tads.manutencao.model.HistoricoSolicitacao;
import br.ufpr.tads.manutencao.model.Perfil;
import br.ufpr.tads.manutencao.model.Solicitacao;
import br.ufpr.tads.manutencao.model.Usuario;
import br.ufpr.tads.manutencao.repository.CategoriaRepository;
import br.ufpr.tads.manutencao.repository.SolicitacaoRepository;
import br.ufpr.tads.manutencao.repository.UsuarioRepository;
import br.ufpr.tads.manutencao.security.UsuarioAutenticado;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceHistoricoTest {

    @Mock
    private SolicitacaoRepository solicitacaoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HistoricoSolicitacaoService historicoSolicitacaoService;

    @Spy
    private SolicitacaoMapper solicitacaoMapper = new SolicitacaoMapper();

    @InjectMocks
    private SolicitacaoService solicitacaoService;

    private Usuario usuarioCliente;
    private Usuario funcionarioMaria;
    private UsuarioAutenticado authCliente;
    private Categoria categoriaNotebook;
    private Solicitacao solicitacaoOrcada;

    @BeforeEach
    void setUp() {
        usuarioCliente = new Usuario("João", "joao@example.com", "hash", "salt", Perfil.CLIENTE);
        usuarioCliente.setId(3L);

        funcionarioMaria = new Usuario("Maria", "maria@example.com", "hash", "salt", Perfil.FUNCIONARIO);
        funcionarioMaria.setId(1L);

        authCliente = new UsuarioAutenticado(3L, "joao@example.com", "CLIENTE", 10L, null);

        categoriaNotebook = new Categoria("Notebook");
        categoriaNotebook.setId(1L);

        solicitacaoOrcada = new Solicitacao();
        solicitacaoOrcada.setId(100L);
        solicitacaoOrcada.setClienteId(10L);
        solicitacaoOrcada.setCategoria(categoriaNotebook);
        solicitacaoOrcada.setDescricaoEquipamento("Notebook Dell");
        solicitacaoOrcada.setDescricaoDefeito("Teclado quebrado");
        solicitacaoOrcada.setEstado(EstadoSolicitacao.ORCADA);
        solicitacaoOrcada.setValorOrcamento(new BigDecimal("250.00"));
        solicitacaoOrcada.setDataHora(LocalDateTime.now().minusDays(1));
    }

    @Test
    @DisplayName("Criar solicitação grava nulo->ABERTA com cliente como responsável")
    void criarSolicitacaoGravaNuloParaAberta() {
        SolicitacaoRequest request = new SolicitacaoRequest();
        request.setCategoriaId(1L);
        request.setDescricaoEquipamento("Impressora HP");
        request.setDescricaoDefeito("Atolando papel");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaNotebook));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuarioCliente));
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenAnswer(inv -> {
            Solicitacao s = inv.getArgument(0);
            s.setId(101L);
            return s;
        });

        SolicitacaoResponse response = solicitacaoService.criarSolicitacao(request, authCliente);

        assertNotNull(response);
        assertEquals("ABERTA", response.getEstado());
        assertEquals(10L, response.getClienteId());

        verify(historicoSolicitacaoService).registrar(
                any(Solicitacao.class),
                eq(null),
                eq(EstadoSolicitacao.ABERTA),
                eq(usuarioCliente),
                eq(null),
                eq(null),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("Aprovar grava ORCADA->APROVADA")
    void aprovarGravaOrcadaParaAprovada() {
        when(solicitacaoRepository.findById(100L)).thenReturn(Optional.of(solicitacaoOrcada));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuarioCliente));
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitacaoResponse response = solicitacaoService.aprovarServico(100L, authCliente);

        assertNotNull(response);
        assertEquals("APROVADA", response.getEstado());

        verify(historicoSolicitacaoService).registrar(
                any(Solicitacao.class),
                eq(EstadoSolicitacao.ORCADA),
                eq(EstadoSolicitacao.APROVADA),
                eq(usuarioCliente),
                eq(null),
                eq(null),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("Rejeitar com motivo grava observação com sucesso")
    void rejeitarComMotivoGravaObservacao() {
        when(solicitacaoRepository.findById(100L)).thenReturn(Optional.of(solicitacaoOrcada));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuarioCliente));
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitacaoResponse response = solicitacaoService.rejeitarServico(100L, "Muito caro", authCliente);

        assertNotNull(response);
        assertEquals("REJEITADA", response.getEstado());
        assertEquals("Muito caro", response.getMotivoRejeicao());

        verify(historicoSolicitacaoService).registrar(
                any(Solicitacao.class),
                eq(EstadoSolicitacao.ORCADA),
                eq(EstadoSolicitacao.REJEITADA),
                eq(usuarioCliente),
                eq(null),
                eq("Muito caro"),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("Transição inválida (ex.: ABERTA->PAGA) falha e NÃO altera solicitação nem grava histórico")
    void transicaoInvalidaFalhaENaoAltera() {
        solicitacaoOrcada.setEstado(EstadoSolicitacao.ABERTA);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                solicitacaoService.alterarEstado(solicitacaoOrcada, EstadoSolicitacao.PAGA, funcionarioMaria, null, null));

        assertEquals("Transição de estado inválida: de ABERTA para PAGA", ex.getMessage());
        assertEquals(EstadoSolicitacao.ABERTA, solicitacaoOrcada.getEstado());

        verify(solicitacaoRepository, never()).save(any());
        verify(historicoSolicitacaoService, never()).registrar(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Falha ao registrar histórico propaga exceção e aborta alteração")
    void falhaAoGravarHistoricoAbortaAlteracao() {
        when(historicoSolicitacaoService.registrar(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Falha simulada no histórico"));

        assertThrows(IllegalArgumentException.class, () ->
                solicitacaoService.alterarEstado(solicitacaoOrcada, EstadoSolicitacao.APROVADA, usuarioCliente, null, null));

        verify(historicoSolicitacaoService).registrar(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("BuscarPorId devolve histórico completo com novos campos mapeados")
    void buscarPorIdDevolveHistoricoCompleto() {
        when(solicitacaoRepository.findById(100L)).thenReturn(Optional.of(solicitacaoOrcada));

        HistoricoSolicitacao h1 = new HistoricoSolicitacao(
                solicitacaoOrcada, null, EstadoSolicitacao.ABERTA,
                LocalDateTime.now().minusDays(2), usuarioCliente, null, null);
        h1.setId(10L);

        HistoricoSolicitacao h2 = new HistoricoSolicitacao(
                solicitacaoOrcada, EstadoSolicitacao.ABERTA, EstadoSolicitacao.ORCADA,
                LocalDateTime.now().minusDays(1), funcionarioMaria, null, null);
        h2.setId(11L);

        when(historicoSolicitacaoService.listar(100L)).thenReturn(List.of(h1, h2));

        SolicitacaoResponse response = solicitacaoService.buscarPorId(100L, authCliente);

        assertNotNull(response);
        assertNotNull(response.getHistorico());
        assertEquals(2, response.getHistorico().size());

        assertEquals(10L, response.getHistorico().get(0).getId());
        assertNull(response.getHistorico().get(0).getEstadoAnterior());
        assertEquals("ABERTA", response.getHistorico().get(0).getEstado());
        assertEquals("João", response.getHistorico().get(0).getResponsavel().getNome());
        assertEquals("CLIENTE", response.getHistorico().get(0).getResponsavel().getPerfil());

        assertEquals(11L, response.getHistorico().get(1).getId());
        assertEquals("ABERTA", response.getHistorico().get(1).getEstadoAnterior());
        assertEquals("ORCADA", response.getHistorico().get(1).getEstado());
        assertEquals("Maria", response.getHistorico().get(1).getResponsavel().getNome());
        assertEquals("FUNCIONARIO", response.getHistorico().get(1).getResponsavel().getPerfil());
    }
}

