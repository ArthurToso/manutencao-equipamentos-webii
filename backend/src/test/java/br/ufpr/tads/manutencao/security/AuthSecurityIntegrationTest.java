package br.ufpr.tads.manutencao.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.ufpr.tads.manutencao.dto.CategoriaRequest;
import br.ufpr.tads.manutencao.dto.CategoriaResponse;
import br.ufpr.tads.manutencao.dto.ClienteResponseDTO;
import br.ufpr.tads.manutencao.model.Cliente;
import br.ufpr.tads.manutencao.model.Funcionario;
import br.ufpr.tads.manutencao.model.Perfil;
import br.ufpr.tads.manutencao.model.Usuario;
import br.ufpr.tads.manutencao.repository.ClienteRepository;
import br.ufpr.tads.manutencao.repository.FuncionarioRepository;
import br.ufpr.tads.manutencao.repository.UsuarioRepository;
import br.ufpr.tads.manutencao.service.CategoriaService;
import br.ufpr.tads.manutencao.service.ClienteService;
import br.ufpr.tads.manutencao.util.SenhaUtils;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @MockitoBean
    private FuncionarioRepository funcionarioRepository;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private ClienteService clienteService;

    private Usuario funcionarioUsuario;
    private Usuario clienteUsuario;
    private Usuario usuarioInativo;

    private final String salt1 = "11112222333344445555666677778888";
    private final String salt2 = "aaaabbbbccccddddeeeeffff00001111";

    @BeforeEach
    void setUp() {
        funcionarioUsuario = new Usuario(
                "Maria",
                "maria@example.com",
                SenhaUtils.gerarHashSha256ComSalt("senha123", salt1),
                salt1,
                Perfil.FUNCIONARIO
        );
        funcionarioUsuario.setId(1L);

        clienteUsuario = new Usuario(
                "João",
                "joao@example.com",
                SenhaUtils.gerarHashSha256ComSalt("1234", salt2),
                salt2,
                Perfil.CLIENTE
        );
        clienteUsuario.setId(2L);

        usuarioInativo = new Usuario(
                "Inativo",
                "inativo@example.com",
                SenhaUtils.gerarHashSha256ComSalt("senha123", salt1),
                salt1,
                Perfil.CLIENTE
        );
        usuarioInativo.setId(3L);
        usuarioInativo.setAtivo(false);
    }

    @Test
    @DisplayName("login maria@example.com/senha123 -> 200, perfil FUNCIONARIO com funcionarioId")
    void deveFazerLoginFuncionarioComSucesso() throws Exception {
        when(usuarioRepository.findByEmailIgnoreCase("maria@example.com"))
                .thenReturn(Optional.of(funcionarioUsuario));

        Funcionario f = new Funcionario();
        f.setId(10L);
        when(funcionarioRepository.findByUsuarioId(1L)).thenReturn(Optional.of(f));

        String requestJson = """
                {
                    "email": "maria@example.com",
                    "senha": "senha123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.id").value(1))
                .andExpect(jsonPath("$.usuario.email").value("maria@example.com"))
                .andExpect(jsonPath("$.usuario.perfil").value("FUNCIONARIO"))
                .andExpect(jsonPath("$.usuario.funcionarioId").value(10))
                .andExpect(jsonPath("$.usuario.clienteId").doesNotExist());
    }

    @Test
    @DisplayName("login joao@example.com/1234 -> 200, perfil CLIENTE com clienteId")
    void deveFazerLoginClienteComSucesso() throws Exception {
        when(usuarioRepository.findByEmailIgnoreCase("joao@example.com"))
                .thenReturn(Optional.of(clienteUsuario));

        Cliente c = new Cliente();
        c.setId(20L);
        when(clienteRepository.findByUsuarioId(2L)).thenReturn(Optional.of(c));

        String requestJson = """
                {
                    "email": "joao@example.com",
                    "senha": "1234"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.id").value(2))
                .andExpect(jsonPath("$.usuario.email").value("joao@example.com"))
                .andExpect(jsonPath("$.usuario.perfil").value("CLIENTE"))
                .andExpect(jsonPath("$.usuario.clienteId").value(20))
                .andExpect(jsonPath("$.usuario.funcionarioId").doesNotExist());
    }

    @Test
    @DisplayName("senha errada / e-mail inexistente / usuário inativo -> 401 com a mesma mensagem")
    void deveRetornar401ComMesmaMensagemParaFalhasDeLogin() throws Exception {
        when(usuarioRepository.findByEmailIgnoreCase("maria@example.com"))
                .thenReturn(Optional.of(funcionarioUsuario));
        when(usuarioRepository.findByEmailIgnoreCase("inexistente@example.com"))
                .thenReturn(Optional.empty());
        when(usuarioRepository.findByEmailIgnoreCase("inativo@example.com"))
                .thenReturn(Optional.of(usuarioInativo));

        // 1. Senha errada
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"maria@example.com\",\"senha\":\"senhaErrada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos"));

        // 2. E-mail inexistente
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"inexistente@example.com\",\"senha\":\"1234\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos"));

        // 3. Usuário inativo
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"inativo@example.com\",\"senha\":\"senha123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos"));
    }

    @Test
    @DisplayName("GET /api/categorias sem token -> 401; com token de CLIENTE -> 200; POST /api/categorias com token de CLIENTE -> 403")
    void deveControlarAcessoEmCategoriasPorPerfilEToken() throws Exception {
        when(categoriaService.listar()).thenReturn(List.of());

        CategoriaResponse catResponse = new CategoriaResponse();
        catResponse.setId(1L);
        catResponse.setNome("Notebook");
        when(categoriaService.criar(any(CategoriaRequest.class))).thenReturn(catResponse);

        String tokenCliente = jwtService.gerarToken(2L, "joao@example.com", "CLIENTE", 20L, null);
        String tokenFuncionario = jwtService.gerarToken(1L, "maria@example.com", "FUNCIONARIO", null, 10L);

        // 1. GET /api/categorias sem token -> 401
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro").value("Não Autorizado"))
                .andExpect(jsonPath("$.mensagem").value("Token de autenticação ausente, expirado ou inválido"));

        // 2. GET /api/categorias com token CLIENTE -> 200
        mockMvc.perform(get("/api/categorias")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk());

        // 3. POST /api/categorias com token CLIENTE -> 403
        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Nova Categoria\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso Proibido"))
                .andExpect(jsonPath("$.mensagem").value("Você não tem permissão para acessar este recurso"));

        // 4. POST /api/categorias com token FUNCIONARIO -> 201
        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + tokenFuncionario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Nova Categoria\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Notebook"));
    }

    @Test
    @DisplayName("POST /api/autocadastro sem token continua 201")
    void devePermitirAutocadastroSemToken() throws Exception {
        ClienteResponseDTO responseDTO = new ClienteResponseDTO();
        responseDTO.setId(100L);
        responseDTO.setNome("Novo Cliente");
        responseDTO.setEmail("novo@example.com");

        when(clienteService.autocadastro(any())).thenReturn(responseDTO);

        String payload = """
                {
                    "nome": "Novo Cliente",
                    "cpf": "11144477735",
                    "email": "novo@example.com",
                    "telefone": "41999998888",
                    "endereco": {
                        "cep": "80010000",
                        "logradouro": "Rua Teste",
                        "numero": "100",
                        "bairro": "Centro",
                        "cidade": "Curitiba",
                        "uf": "PR"
                    }
                }
                """;

        mockMvc.perform(post("/api/autocadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.email").value("novo@example.com"));
    }

    @Test
    @DisplayName("token adulterado -> 401 em JSON")
    void deveRetornar401JsonQuandoTokenForAdulterado() throws Exception {
        String tokenValido = jwtService.gerarToken(2L, "joao@example.com", "CLIENTE", 20L, null);
        String tokenAdulterado = tokenValido.substring(0, tokenValido.length() - 5) + "abcde";

        mockMvc.perform(get("/api/categorias")
                        .header("Authorization", "Bearer " + tokenAdulterado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro").value("Não Autorizado"))
                .andExpect(jsonPath("$.mensagem").value("Token de autenticação ausente, expirado ou inválido"));
    }
}

