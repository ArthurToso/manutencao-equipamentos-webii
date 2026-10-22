package br.ufpr.tads.manutencao.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.ufpr.tads.manutencao.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HistoricoSolicitacaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenJoaoCliente;
    private String tokenJoseCliente;
    private String tokenMariaFuncionario;

    @BeforeEach
    void setUp() {
        // João é CLIENTE (usuario_id = 3, cliente_id = 1)
        tokenJoaoCliente = jwtService.gerarToken(3L, "joao@example.com", "CLIENTE", 1L, null);

        // José é CLIENTE (usuario_id = 4, cliente_id = 2)
        tokenJoseCliente = jwtService.gerarToken(4L, "jose@example.com", "CLIENTE", 2L, null);

        // Maria é FUNCIONARIO (usuario_id = 1, funcionario_id = 1)
        tokenMariaFuncionario = jwtService.gerarToken(1L, "maria@example.com", "FUNCIONARIO", null, 1L);
    }

    @Test
    @DisplayName("Critério 5: Criar solicitação -> GET /api/solicitacoes/{id} mostra 1 linha (estadoAnterior nulo, estado ABERTA, responsavel = cliente)")
    void criarSolicitacaoEMostrarUmaLinhaDeHistorico() throws Exception {
        String payload = """
                {
                    "categoriaId": 1,
                    "descricaoEquipamento": "Notebook Acer Aspire",
                    "descricaoDefeito": "Tela piscando"
                }
                """;

        MvcResult postResult = mockMvc.perform(post("/api/solicitacoes")
                        .header("Authorization", "Bearer " + tokenJoaoCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado").value("ABERTA"))
                .andReturn();

        JsonNode postJson = objectMapper.readTree(postResult.getResponse().getContentAsString());
        long solicitacaoId = postJson.get("id").asLong();

        mockMvc.perform(get("/api/solicitacoes/" + solicitacaoId)
                        .header("Authorization", "Bearer " + tokenJoaoCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(solicitacaoId))
                .andExpect(jsonPath("$.historico", hasSize(1)))
                .andExpect(jsonPath("$.historico[0].estadoAnterior", nullValue()))
                .andExpect(jsonPath("$.historico[0].estado").value("ABERTA"))
                .andExpect(jsonPath("$.historico[0].responsavel.id").value(3))
                .andExpect(jsonPath("$.historico[0].responsavel.nome").value("João"))
                .andExpect(jsonPath("$.historico[0].responsavel.perfil").value("CLIENTE"))
                .andExpect(jsonPath("$.historico[0].nomeFuncionario").value("João"));
    }

    @Test
    @DisplayName("Critério 7a: GET na solicitação 10 mostra a linha APROVADA com estadoAnterior REJEITADA")
    void getSolicitacao10MostraLinhaAprovadaComEstadoAnteriorRejeitada() throws Exception {
        // Solicitação 10 pertence ao cliente 2 (José)
        mockMvc.perform(get("/api/solicitacoes/10")
                        .header("Authorization", "Bearer " + tokenJoseCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.estado").value("APROVADA"))
                .andExpect(jsonPath("$.historico", hasSize(4)))
                // Linha 4 (id 48): REJEITADA -> APROVADA
                .andExpect(jsonPath("$.historico[3].estadoAnterior").value("REJEITADA"))
                .andExpect(jsonPath("$.historico[3].estado").value("APROVADA"))
                .andExpect(jsonPath("$.historico[3].responsavel.nome").value("José"));
    }

    @Test
    @DisplayName("Critério 7b: GET na solicitação 3 mostra a REDIRECIONADA com funcionarioDestino Mário")
    void getSolicitacao3MostraRedirecionadaComFuncionarioDestinoMario() throws Exception {
        // Funcionário Maria pode ler a solicitação 3
        mockMvc.perform(get("/api/solicitacoes/3")
                        .header("Authorization", "Bearer " + tokenMariaFuncionario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.historico", hasSize(7)))
                // Linha 4 (id 16): REDIRECIONADA por Maria (1) para Mário (2)
                .andExpect(jsonPath("$.historico[3].estadoAnterior").value("APROVADA"))
                .andExpect(jsonPath("$.historico[3].estado").value("REDIRECIONADA"))
                .andExpect(jsonPath("$.historico[3].responsavel.nome").value("Maria"))
                .andExpect(jsonPath("$.historico[3].funcionarioDestino.id").value(2))
                .andExpect(jsonPath("$.historico[3].funcionarioDestino.nome").value("Mário"));
    }

    @Test
    @DisplayName("Critério 6: Rejeitar sem motivo é recusado (400) e não altera o estado")
    void rejeitarSemMotivoERecusadoENaoAlteraEstado() throws Exception {
        // Solicitação 14 está ORCADA e pertence ao cliente 2 (José)
        mockMvc.perform(put("/api/solicitacoes/14/rejeitar")
                        .header("Authorization", "Bearer " + tokenJoseCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"\"}"))
                .andExpect(status().isBadRequest());

        // Verifica que o estado continuou ORCADA
        mockMvc.perform(get("/api/solicitacoes/14")
                        .header("Authorization", "Bearer " + tokenJoseCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ORCADA"))
                .andExpect(jsonPath("$.historico", hasSize(2)));
    }

    @Test
    @DisplayName("Critério 6: Aprovar grava linha APROVADA e altera estado com sucesso")
    void aprovarGravaLinhaAprovadaComSucesso() throws Exception {
        // Solicitação 14 está ORCADA e pertence ao cliente 2 (José)
        mockMvc.perform(put("/api/solicitacoes/14/aprovar")
                        .header("Authorization", "Bearer " + tokenJoseCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROVADA"));

        mockMvc.perform(get("/api/solicitacoes/14")
                        .header("Authorization", "Bearer " + tokenJoseCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROVADA"))
                .andExpect(jsonPath("$.historico", hasSize(3)))
                .andExpect(jsonPath("$.historico[2].estadoAnterior").value("ORCADA"))
                .andExpect(jsonPath("$.historico[2].estado").value("APROVADA"))
                .andExpect(jsonPath("$.historico[2].responsavel.nome").value("José"));
    }

    @Test
    @DisplayName("Critério 6: Rejeitar com motivo grava linha REJEITADA com observação")
    void rejeitarComMotivoGravaLinhaRejeitadaComObservacao() throws Exception {
        // Solicitação 14 está ORCADA e pertence ao cliente 2 (José)
        mockMvc.perform(put("/api/solicitacoes/14/rejeitar")
                        .header("Authorization", "Bearer " + tokenJoseCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Valor fora do orçamento planejado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REJEITADA"))
                .andExpect(jsonPath("$.motivoRejeicao").value("Valor fora do orçamento planejado"));

        mockMvc.perform(get("/api/solicitacoes/14")
                        .header("Authorization", "Bearer " + tokenJoseCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REJEITADA"))
                .andExpect(jsonPath("$.historico", hasSize(3)))
                .andExpect(jsonPath("$.historico[2].estadoAnterior").value("ORCADA"))
                .andExpect(jsonPath("$.historico[2].estado").value("REJEITADA"))
                .andExpect(jsonPath("$.historico[2].observacao").value("Valor fora do orçamento planejado"))
                .andExpect(jsonPath("$.historico[2].responsavel.nome").value("José"));
    }
}

