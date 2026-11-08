package br.ufpr.tads.manutencao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import br.ufpr.tads.manutencao.dto.SolicitacaoResponse;
import br.ufpr.tads.manutencao.model.EstadoSolicitacao;
import br.ufpr.tads.manutencao.repository.CategoriaRepository;
import br.ufpr.tads.manutencao.repository.ClienteRepository;
import br.ufpr.tads.manutencao.repository.SolicitacaoRepository;
import br.ufpr.tads.manutencao.repository.UsuarioRepository;
import br.ufpr.tads.manutencao.security.UsuarioAutenticado;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceVisualizacaoTest {

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

    @InjectMocks
    private SolicitacaoService solicitacaoService;

    private UsuarioAutenticado authMariaFuncionario;
    private UsuarioAutenticado authMarioFuncionario;
    private UsuarioAutenticado authJoaoCliente;

    private SolicitacaoResponse respAberta;
    private SolicitacaoResponse respOrcada;
    private SolicitacaoResponse respRedirecionadaMaria;
    private SolicitacaoResponse respRedirecionadaMario;

    @BeforeEach
    void setUp() {
        authMariaFuncionario = new UsuarioAutenticado(1L, "maria@example.com", "FUNCIONARIO", null, 1L);
        authMarioFuncionario = new UsuarioAutenticado(2L, "mario@example.com", "FUNCIONARIO", null, 2L);
        authJoaoCliente = new UsuarioAutenticado(3L, "joao@example.com", "CLIENTE", 1L, null);

        respAberta = new SolicitacaoResponse(
                17L,
                LocalDateTime.of(2026, 10, 6, 10, 10),
                "Impressora Canon PIXMA G3110",
                "Impressão sai em branco",
                EstadoSolicitacao.ABERTA,
                3L,
                "Impressora",
                1L,
                "João",
                null,
                null
        );

        respOrcada = new SolicitacaoResponse(
                14L,
                LocalDateTime.of(2026, 9, 29, 16, 20),
                "Teclado Microsoft Wired 600",
                "Líquido derramado",
                EstadoSolicitacao.ORCADA,
                5L,
                "Teclado",
                2L,
                "José",
                new BigDecimal("70.00"),
                null
        );

        respRedirecionadaMaria = new SolicitacaoResponse(
                13L,
                LocalDateTime.of(2026, 9, 25, 8, 30),
                "Desktop HP EliteDesk 800 G4",
                "Não dá vídeo",
                EstadoSolicitacao.REDIRECIONADA,
                2L,
                "Desktop",
                1L,
                "João",
                new BigDecimal("260.00"),
                null
        );

        respRedirecionadaMario = new SolicitacaoResponse(
                12L,
                LocalDateTime.of(2026, 9, 22, 14, 0),
                "Multifuncional Brother DCP-T420W",
                "Scanner não reconhecido",
                EstadoSolicitacao.REDIRECIONADA,
                3L,
                "Impressora",
                4L,
                "Joaquina",
                new BigDecimal("210.00"),
                null
        );
    }

    @Test
    @DisplayName("Filtro TODAS busca todas as solicitações para o funcionário logado")
    void filtroTodas_chamaListarTodasParaFuncionario() {
        when(solicitacaoRepository.listarTodasParaFuncionario(1L))
                .thenReturn(List.of(respRedirecionadaMaria, respOrcada, respAberta));

        List<SolicitacaoResponse> resultado = solicitacaoService.listarParaFuncionario("TODAS", null, null, authMariaFuncionario);

        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        verify(solicitacaoRepository).listarTodasParaFuncionario(1L);
    }

    @Test
    @DisplayName("Filtro HOJE busca intervalo inclusivo do dia atual [00:00, 00:00 do dia seguinte)")
    void filtroHoje_chamaListarPorPeriodoComInicioEFimDeHoje() {
        LocalDate hoje = LocalDate.now();
        when(solicitacaoRepository.listarPorPeriodoParaFuncionario(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<SolicitacaoResponse> resultado = solicitacaoService.listarParaFuncionario("HOJE", null, null, authMariaFuncionario);

        assertNotNull(resultado);
        ArgumentCaptor<LocalDateTime> captorInicio = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> captorFim = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(solicitacaoRepository).listarPorPeriodoParaFuncionario(eq(1L), captorInicio.capture(), captorFim.capture());

        assertEquals(hoje.atStartOfDay(), captorInicio.getValue());
        assertEquals(hoje.plusDays(1).atStartOfDay(), captorFim.getValue());
    }

    @Test
    @DisplayName("Filtro PERIODO com inicio e fim busca intervalo com fim no início do dia seguinte")
    void filtroPeriodo_comInicioEFim_chamaListarPorPeriodo() {
        LocalDate inicio = LocalDate.of(2026, 9, 1);
        LocalDate fim = LocalDate.of(2026, 9, 30);

        when(solicitacaoRepository.listarPorPeriodoParaFuncionario(eq(1L), eq(inicio.atStartOfDay()), eq(fim.plusDays(1).atStartOfDay())))
                .thenReturn(List.of(respRedirecionadaMaria, respOrcada));

        List<SolicitacaoResponse> resultado = solicitacaoService.listarParaFuncionario("PERIODO", inicio, fim, authMariaFuncionario);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(solicitacaoRepository).listarPorPeriodoParaFuncionario(1L, inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay());
    }

    @Test
    @DisplayName("Filtro PERIODO com apenas dataInicial busca intervalo aberto à direita")
    void filtroPeriodo_apenasDataInicial_chamaListarAposData() {
        LocalDate inicio = LocalDate.of(2026, 9, 1);

        when(solicitacaoRepository.listarAposDataParaFuncionario(1L, inicio.atStartOfDay()))
                .thenReturn(List.of(respRedirecionadaMaria));

        List<SolicitacaoResponse> resultado = solicitacaoService.listarParaFuncionario("PERIODO", inicio, null, authMariaFuncionario);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(solicitacaoRepository).listarAposDataParaFuncionario(1L, inicio.atStartOfDay());
    }

    @Test
    @DisplayName("Filtro PERIODO com apenas dataFinal busca intervalo aberto à esquerda")
    void filtroPeriodo_apenasDataFinal_chamaListarAteData() {
        LocalDate fim = LocalDate.of(2026, 9, 30);

        when(solicitacaoRepository.listarAteDataParaFuncionario(1L, fim.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(respRedirecionadaMaria));

        List<SolicitacaoResponse> resultado = solicitacaoService.listarParaFuncionario("PERIODO", null, fim, authMariaFuncionario);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(solicitacaoRepository).listarAteDataParaFuncionario(1L, fim.plusDays(1).atStartOfDay());
    }

    @Test
    @DisplayName("Filtro PERIODO com inicio e fim nulos se comporta como TODAS")
    void filtroPeriodo_inicioEFimNulos_chamaListarTodas() {
        when(solicitacaoRepository.listarTodasParaFuncionario(1L))
                .thenReturn(List.of(respRedirecionadaMaria));

        List<SolicitacaoResponse> resultado = solicitacaoService.listarParaFuncionario("PERIODO", null, null, authMariaFuncionario);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(solicitacaoRepository).listarTodasParaFuncionario(1L);
    }

    @Test
    @DisplayName("Filtro PERIODO com dataInicial posterior a dataFinal lanca IllegalArgumentException")
    void filtroPeriodo_dataInicialPosteriorADataFinal_lancaExcecao() {
        LocalDate inicio = LocalDate.of(2026, 10, 10);
        LocalDate fim = LocalDate.of(2026, 10, 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                solicitacaoService.listarParaFuncionario("PERIODO", inicio, fim, authMariaFuncionario));

        assertEquals("A data inicial não pode ser posterior à data final", ex.getMessage());
    }

    @Test
    @DisplayName("Filtro desconhecido lanca IllegalArgumentException")
    void filtroDesconhecido_lancaExcecao() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                solicitacaoService.listarParaFuncionario("INVALIDO", null, null, authMariaFuncionario));

        assertEquals("Filtro inválido. Use HOJE, PERIODO ou TODAS", ex.getMessage());
    }

    @Test
    @DisplayName("Perfil CLIENTE recebe AccessDeniedException")
    void perfilCliente_lancaAccessDeniedException() {
        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                solicitacaoService.listarParaFuncionario("TODAS", null, null, authJoaoCliente));

        assertEquals("Apenas funcionários podem visualizar solicitações", ex.getMessage());
    }

    @Test
    @DisplayName("Solicitacao REDIRECIONADA para Maria aparece para Maria e nao para Mario")
    void solicitacaoRedirecionada_visibilidadePorFuncionarioDestino() {
        when(solicitacaoRepository.listarTodasParaFuncionario(1L))
                .thenReturn(List.of(respRedirecionadaMaria, respAberta));
        when(solicitacaoRepository.listarTodasParaFuncionario(2L))
                .thenReturn(List.of(respRedirecionadaMario, respAberta));

        List<SolicitacaoResponse> solicitacoesMaria = solicitacaoService.listarParaFuncionario("TODAS", null, null, authMariaFuncionario);
        List<SolicitacaoResponse> solicitacoesMario = solicitacaoService.listarParaFuncionario("TODAS", null, null, authMarioFuncionario);

        assertTrue(solicitacoesMaria.stream().anyMatch(s -> s.getId().equals(13L)));
        assertTrue(solicitacoesMaria.stream().noneMatch(s -> s.getId().equals(12L)));

        assertTrue(solicitacoesMario.stream().anyMatch(s -> s.getId().equals(12L)));
        assertTrue(solicitacoesMario.stream().noneMatch(s -> s.getId().equals(13L)));

        assertTrue(solicitacoesMaria.stream().anyMatch(s -> s.getId().equals(17L)));
        assertTrue(solicitacoesMario.stream().anyMatch(s -> s.getId().equals(17L)));
    }

    @Test
    @DisplayName("As solicitacoes sao retornadas em ordem crescente por dataHora")
    void solicitacoesRetornadas_emOrdemCrescentePorDataHora() {
        when(solicitacaoRepository.listarTodasParaFuncionario(1L))
                .thenReturn(List.of(respRedirecionadaMaria, respOrcada, respAberta));

        List<SolicitacaoResponse> resultado = solicitacaoService.listarParaFuncionario("TODAS", null, null, authMariaFuncionario);

        assertEquals(3, resultado.size());
        assertTrue(resultado.get(0).getDataHora().isBefore(resultado.get(1).getDataHora()));
        assertTrue(resultado.get(1).getDataHora().isBefore(resultado.get(2).getDataHora()));
    }
}
