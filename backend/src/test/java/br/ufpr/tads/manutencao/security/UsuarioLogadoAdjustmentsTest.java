package br.ufpr.tads.manutencao.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.ufpr.tads.manutencao.model.Categoria;
import br.ufpr.tads.manutencao.model.EstadoSolicitacao;
import br.ufpr.tads.manutencao.model.Funcionario;
import br.ufpr.tads.manutencao.model.HistoricoSolicitacao;
import br.ufpr.tads.manutencao.model.Perfil;
import br.ufpr.tads.manutencao.model.Solicitacao;
import br.ufpr.tads.manutencao.model.Usuario;
import br.ufpr.tads.manutencao.repository.CategoriaRepository;
import br.ufpr.tads.manutencao.repository.FuncionarioRepository;
import br.ufpr.tads.manutencao.repository.HistoricoSolicitacaoRepository;
import br.ufpr.tads.manutencao.repository.SolicitacaoRepository;
import br.ufpr.tads.manutencao.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioLogadoAdjustmentsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private SolicitacaoRepository solicitacaoRepository;

    @MockitoBean
    private CategoriaRepository categoriaRepository;

    @MockitoBean
    private HistoricoSolicitacaoRepository historicoSolicitacaoRepository;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private br.ufpr.tads.manutencao.service.FuncionarioService funcionarioService;

    private String tokenJoaoCliente;
    private String tokenMariaFuncionario;

    private Solicitacao solicitacaoDoJoao;
    private Solicitacao solicitacaoDeOutroCliente;
    private Usuario usuarioJoao;
    private Usuario usuarioMaria;

    @BeforeEach
    void setUp() {
        // João é CLIENTE com usuarioId=2 e clienteId=20
        tokenJoaoCliente = jwtService.gerarToken(2L, "joao@example.com", "CLIENTE", 20L, null);

        // Maria é FUNCIONARIO com usuarioId=1 e funcionarioId=10
        tokenMariaFuncionario = jwtService.gerarToken(1L, "maria@example.com", "FUNCIONARIO", null, 10L);

        usuarioJoao = new Usuario("João", "joao@example.com", "hash", "salt", Perfil.CLIENTE);
        usuarioJoao.setId(2L);

        usuarioMaria = new Usuario("Maria", "maria@example.com", "hash", "salt", Perfil.FUNCIONARIO);
        usuarioMaria.setId(1L);

        Categoria categoria = new Categoria("Notebook");
        categoria.setId(1L);

        solicitacaoDoJoao = new Solicitacao();
        solicitacaoDoJoao.setId(100L);
        solicitacaoDoJoao.setDescricaoEquipamento("Notebook Dell");
        solicitacaoDoJoao.setDescricaoDefeito("Teclado falhando");
        solicitacaoDoJoao.setCategoria(categoria);
        solicitacaoDoJoao.setClienteId(20L); // Pertence ao João
        solicitacaoDoJoao.setEstado(EstadoSolicitacao.ORCADA);
        solicitacaoDoJoao.setDataHora(LocalDateTime.now());

        solicitacaoDeOutroCliente = new Solicitacao();
        solicitacaoDeOutroCliente.setId(200L);
        solicitacaoDeOutroCliente.setDescricaoEquipamento("Desktop Gamer");
        solicitacaoDeOutroCliente.setDescricaoDefeito("Não liga");
        solicitacaoDeOutroCliente.setCategoria(categoria);
        solicitacaoDeOutroCliente.setClienteId(99L); // Pertence a outro cliente
        solicitacaoDeOutroCliente.setEstado(EstadoSolicitacao.ORCADA);
        solicitacaoDeOutroCliente.setDataHora(LocalDateTime.now());
    }

    @Test
    @DisplayName("João não lê solicitação de outro cliente (403)")
    void joaoNaoLeSolicitacaoDeOutroCliente() throws Exception {
        when(solicitacaoRepository.findById(200L)).thenReturn(Optional.of(solicitacaoDeOutroCliente));

        mockMvc.perform(get("/api/solicitacoes/200")
                        .header("Authorization", "Bearer " + tokenJoaoCliente))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso Proibido"))
                .andExpect(jsonPath("$.mensagem").value("Você não tem permissão para acessar esta solicitação"));
    }

    @Test
    @DisplayName("João não aprova solicitação de outro cliente (403)")
    void joaoNaoAprovaSolicitacaoDeOutroCliente() throws Exception {
        when(solicitacaoRepository.findById(200L)).thenReturn(Optional.of(solicitacaoDeOutroCliente));

        mockMvc.perform(put("/api/solicitacoes/200/aprovar")
                        .header("Authorization", "Bearer " + tokenJoaoCliente))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso Proibido"))
                .andExpect(jsonPath("$.mensagem").value("Você não tem permissão para acessar esta solicitação"));
    }

    @Test
    @DisplayName("João não rejeita solicitação de outro cliente (403)")
    void joaoNaoRejeitaSolicitacaoDeOutroCliente() throws Exception {
        when(solicitacaoRepository.findById(200L)).thenReturn(Optional.of(solicitacaoDeOutroCliente));

        mockMvc.perform(put("/api/solicitacoes/200/rejeitar")
                        .header("Authorization", "Bearer " + tokenJoaoCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Valor muito alto\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso Proibido"));
    }

    @Test
    @DisplayName("João não busca orçamento de solicitação de outro cliente (403)")
    void joaoNaoBuscaOrcamentoDeOutroCliente() throws Exception {
        when(solicitacaoRepository.findById(200L)).thenReturn(Optional.of(solicitacaoDeOutroCliente));

        mockMvc.perform(get("/api/solicitacoes/200/orcamento")
                        .header("Authorization", "Bearer " + tokenJoaoCliente))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso Proibido"));
    }

    @Test
    @DisplayName("João lê sua própria solicitação (200)")
    void joaoLeSuaPropriaSolicitacao() throws Exception {
        when(solicitacaoRepository.findById(100L)).thenReturn(Optional.of(solicitacaoDoJoao));
        when(historicoSolicitacaoRepository.findBySolicitacaoIdOrderByDataHoraAsc(100L)).thenReturn(List.of());

        mockMvc.perform(get("/api/solicitacoes/100")
                        .header("Authorization", "Bearer " + tokenJoaoCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.clienteId").value(20));
    }

    @Test
    @DisplayName("João consulta suas próprias solicitações via /api/solicitacoes/minhas (200)")
    void joaoConsultaMinhasSolicitacoes() throws Exception {
        when(solicitacaoRepository.findByClienteIdOrderByDataHoraAsc(20L))
                .thenReturn(List.of(solicitacaoDoJoao));

        mockMvc.perform(get("/api/solicitacoes/minhas")
                        .header("Authorization", "Bearer " + tokenJoaoCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].clienteId").value(20));
    }

    @Test
    @DisplayName("Funcionário lê solicitação de qualquer cliente (200)")
    void funcionarioLeSolicitacaoDeQualquerCliente() throws Exception {
        when(solicitacaoRepository.findById(200L)).thenReturn(Optional.of(solicitacaoDeOutroCliente));
        when(historicoSolicitacaoRepository.findBySolicitacaoIdOrderByDataHoraAsc(200L)).thenReturn(List.of());

        mockMvc.perform(get("/api/solicitacoes/200")
                        .header("Authorization", "Bearer " + tokenMariaFuncionario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(200))
                .andExpect(jsonPath("$.clienteId").value(99));
    }

    @Test
    @DisplayName("Criar solicitação usa o clienteId do token e novo histórico grava o usuário")
    void criarSolicitacaoUsaClienteIdDoTokenEGravaUsuarioNoHistorico() throws Exception {
        Categoria categoria = new Categoria("Notebook");
        categoria.setId(1L);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(usuarioRepository.getReferenceById(2L)).thenReturn(usuarioJoao);

        when(solicitacaoRepository.save(any(Solicitacao.class))).thenAnswer(invocation -> {
            Solicitacao s = invocation.getArgument(0);
            s.setId(300L);
            return s;
        });

        String payload = """
                {
                    "descricaoEquipamento": "Mouse Sem Fio",
                    "categoriaId": 1,
                    "descricaoDefeito": "Botão solto"
                }
                """;

        mockMvc.perform(post("/api/solicitacoes")
                        .header("Authorization", "Bearer " + tokenJoaoCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(300))
                .andExpect(jsonPath("$.clienteId").value(20)); // clienteId do token!

        // Valida que o historico salvou com o usuário responsável (João)
        ArgumentCaptor<HistoricoSolicitacao> historicoCaptor = ArgumentCaptor.forClass(HistoricoSolicitacao.class);
        verify(historicoSolicitacaoRepository).save(historicoCaptor.capture());
        HistoricoSolicitacao historicoSalvo = historicoCaptor.getValue();

        assertNotNull(historicoSalvo.getUsuario());
        assertEquals(2L, historicoSalvo.getUsuario().getId());
        assertEquals("João", historicoSalvo.getUsuario().getNome());
        assertEquals(EstadoSolicitacao.ABERTA, historicoSalvo.getEstado());
    }

    @Test
    @DisplayName("Funcionário não remove a si mesmo (RF018)")
    void funcionarioNaoRemoveASiMesmo() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("Não é possível remover o próprio usuário conectado"))
                .when(funcionarioService).remover(10L);

        // Maria (funcionarioId=10) tenta deletar ela mesma (/api/funcionarios/10)
        mockMvc.perform(delete("/api/funcionarios/10")
                        .header("Authorization", "Bearer " + tokenMariaFuncionario))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Conflito de Estado"))
                .andExpect(jsonPath("$.mensagem").value("Não é possível remover o próprio usuário conectado"));
    }

    @Test
    @DisplayName("Funcionário pode remover outro funcionário se houver mais de um ativo (204)")
    void funcionarioRemoveOutroFuncionario() throws Exception {
        org.mockito.Mockito.doNothing().when(funcionarioService).remover(11L);

        // Maria (funcionarioId=10) deleta Pedro (id=11)
        mockMvc.perform(delete("/api/funcionarios/11")
                        .header("Authorization", "Bearer " + tokenMariaFuncionario))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Cliente não pode remover funcionário (403)")
    void clienteNaoPodeRemoverFuncionario() throws Exception {
        mockMvc.perform(delete("/api/funcionarios/11")
                        .header("Authorization", "Bearer " + tokenJoaoCliente))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso Proibido"));
    }
}

