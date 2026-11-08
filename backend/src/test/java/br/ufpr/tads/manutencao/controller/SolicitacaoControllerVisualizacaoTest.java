package br.ufpr.tads.manutencao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.ufpr.tads.manutencao.dto.SolicitacaoResponse;
import br.ufpr.tads.manutencao.model.EstadoSolicitacao;
import br.ufpr.tads.manutencao.repository.CategoriaRepository;
import br.ufpr.tads.manutencao.repository.ClienteRepository;
import br.ufpr.tads.manutencao.repository.FuncionarioRepository;
import br.ufpr.tads.manutencao.repository.HistoricoSolicitacaoRepository;
import br.ufpr.tads.manutencao.repository.SolicitacaoRepository;
import br.ufpr.tads.manutencao.repository.UsuarioRepository;
import br.ufpr.tads.manutencao.security.JwtService;
import br.ufpr.tads.manutencao.service.SolicitacaoService;

@SpringBootTest
@AutoConfigureMockMvc
class SolicitacaoControllerVisualizacaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private SolicitacaoService solicitacaoService;

    @MockitoBean
    private SolicitacaoRepository solicitacaoRepository;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @MockitoBean
    private FuncionarioRepository funcionarioRepository;

    @MockitoBean
    private CategoriaRepository categoriaRepository;

    @MockitoBean
    private HistoricoSolicitacaoRepository historicoSolicitacaoRepository;

    private String tokenMariaFuncionario;
    private String tokenJoaoCliente;

    @BeforeEach
    void setUp() {
        tokenMariaFuncionario = jwtService.gerarToken(1L, "maria@example.com", "FUNCIONARIO", null, 1L);
        tokenJoaoCliente = jwtService.gerarToken(3L, "joao@example.com", "CLIENTE", 1L, null);
    }

    @Test
    @DisplayName("GET /api/solicitacoes sem token retorna 401")
    void listar_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/solicitacoes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/solicitacoes com perfil CLIENTE retorna 403")
    void listar_comPerfilCliente_retorna403() throws Exception {
        mockMvc.perform(get("/api/solicitacoes")
                        .header("Authorization", "Bearer " + tokenJoaoCliente))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/solicitacoes com perfil FUNCIONARIO retorna 200 e a lista")
    void listar_comPerfilFuncionario_retorna200ELista() throws Exception {
        SolicitacaoResponse item = new SolicitacaoResponse(
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

        when(solicitacaoService.listarParaFuncionario(eq("TODAS"), any(), any(), any()))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/solicitacoes")
                        .header("Authorization", "Bearer " + tokenMariaFuncionario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(14))
                .andExpect(jsonPath("$[0].descricaoEquipamento").value("Teclado Microsoft Wired 600"))
                .andExpect(jsonPath("$[0].nomeCliente").value("José"))
                .andExpect(jsonPath("$[0].estado").value("ORCADA"));
    }

    @Test
    @DisplayName("GET /api/solicitacoes com dataInicial posterior a dataFinal retorna 400")
    void listar_dataInicialMaiorQueDataFinal_retorna400() throws Exception {
        when(solicitacaoService.listarParaFuncionario(eq("PERIODO"), eq(LocalDate.of(2026, 10, 10)), eq(LocalDate.of(2026, 10, 5)), any()))
                .thenThrow(new IllegalArgumentException("A data inicial não pode ser posterior à data final"));

        mockMvc.perform(get("/api/solicitacoes")
                        .header("Authorization", "Bearer " + tokenMariaFuncionario)
                        .param("filtro", "PERIODO")
                        .param("dataInicial", "2026-10-10")
                        .param("dataFinal", "2026-10-05"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A data inicial não pode ser posterior à data final"));
    }
}
