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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import br.ufpr.tads.manutencao.dto.SolicitacaoResponse;
import br.ufpr.tads.manutencao.mapper.SolicitacaoMapper;
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
class SolicitacaoServiceFinalizacaoTest {

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

    private final UsuarioAutenticado funcionarioAutenticado =
            new UsuarioAutenticado(1L, "maria@example.com", "FUNCIONARIO", null, 1L);
    private final UsuarioAutenticado clienteAutenticado =
            new UsuarioAutenticado(3L, "joao@example.com", "CLIENTE", 10L, null);
    private Usuario funcionario;

    @BeforeEach
    void setUp() {
        funcionario = new Usuario("Maria", "maria@example.com", "hash", "salt", Perfil.FUNCIONARIO);
        funcionario.setId(1L);
    }

    @Test
    @DisplayName("RF016: funcionário finaliza solicitação paga e registra histórico")
    void funcionarioFinalizaSolicitacaoPaga() {
        Solicitacao solicitacao = criarSolicitacao(EstadoSolicitacao.PAGA);
        when(solicitacaoRepository.findById(50L)).thenReturn(Optional.of(solicitacao));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(funcionario));
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SolicitacaoResponse response = solicitacaoService.finalizarSolicitacao(50L, funcionarioAutenticado);

        assertNotNull(response);
        assertEquals("FINALIZADA", response.getEstado());
        assertEquals(EstadoSolicitacao.FINALIZADA, solicitacao.getEstado());
        verify(historicoSolicitacaoService).registrar(
                eq(solicitacao),
                eq(EstadoSolicitacao.PAGA),
                eq(EstadoSolicitacao.FINALIZADA),
                eq(funcionario),
                eq(null),
                eq(null),
                any(LocalDateTime.class));
    }

    @Test
    @DisplayName("RF016: cliente não pode finalizar uma solicitação")
    void clienteNaoPodeFinalizarSolicitacao() {
        assertThrows(AccessDeniedException.class,
                () -> solicitacaoService.finalizarSolicitacao(50L, clienteAutenticado));

        verify(solicitacaoRepository, never()).findById(any());
        verify(historicoSolicitacaoService, never()).registrar(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("RF016: solicitação que não está paga não pode ser finalizada")
    void estadoDiferenteDePagoNaoPodeSerFinalizado() {
        Solicitacao solicitacao = criarSolicitacao(EstadoSolicitacao.ARRUMADA);
        when(solicitacaoRepository.findById(50L)).thenReturn(Optional.of(solicitacao));

        assertThrows(IllegalStateException.class,
                () -> solicitacaoService.finalizarSolicitacao(50L, funcionarioAutenticado));

        verify(usuarioRepository, never()).findById(any());
        verify(solicitacaoRepository, never()).save(any(Solicitacao.class));
        verify(historicoSolicitacaoService, never()).registrar(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("RF016: solicitação inexistente retorna erro")
    void solicitacaoInexistenteNaoPodeSerFinalizada() {
        when(solicitacaoRepository.findById(50L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> solicitacaoService.finalizarSolicitacao(50L, funcionarioAutenticado));

        verify(historicoSolicitacaoService, never()).registrar(any(), any(), any(), any(), any(), any(), any());
    }

    private Solicitacao criarSolicitacao(EstadoSolicitacao estado) {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId(50L);
        solicitacao.setEstado(estado);
        solicitacao.setDataHora(LocalDateTime.now().minusDays(1));
        return solicitacao;
    }
}
