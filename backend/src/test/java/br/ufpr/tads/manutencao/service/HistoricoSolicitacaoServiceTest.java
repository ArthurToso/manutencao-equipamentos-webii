package br.ufpr.tads.manutencao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.ufpr.tads.manutencao.model.EstadoSolicitacao;
import br.ufpr.tads.manutencao.model.HistoricoSolicitacao;
import br.ufpr.tads.manutencao.model.Perfil;
import br.ufpr.tads.manutencao.model.Solicitacao;
import br.ufpr.tads.manutencao.model.Usuario;
import br.ufpr.tads.manutencao.repository.HistoricoSolicitacaoRepository;

@ExtendWith(MockitoExtension.class)
class HistoricoSolicitacaoServiceTest {

    @Mock
    private HistoricoSolicitacaoRepository historicoSolicitacaoRepository;

    @InjectMocks
    private HistoricoSolicitacaoService historicoSolicitacaoService;

    private Solicitacao solicitacao;
    private Usuario usuarioCliente;
    private Usuario funcionarioOrigem;
    private Usuario funcionarioDestino;

    @BeforeEach
    void setUp() {
        solicitacao = new Solicitacao();
        solicitacao.setId(1L);

        usuarioCliente = new Usuario("João", "joao@example.com", "hash", "salt", Perfil.CLIENTE);
        usuarioCliente.setId(3L);

        funcionarioOrigem = new Usuario("Maria", "maria@example.com", "hash", "salt", Perfil.FUNCIONARIO);
        funcionarioOrigem.setId(1L);

        funcionarioDestino = new Usuario("Mário", "mario@example.com", "hash", "salt", Perfil.FUNCIONARIO);
        funcionarioDestino.setId(2L);
    }

    @Test
    @DisplayName("Responsável nulo lança IllegalArgumentException")
    void responsavelNuloLancaExcecao() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                historicoSolicitacaoService.registrar(solicitacao, null, EstadoSolicitacao.ABERTA, null, null, null));
        assertEquals("Usuário responsável é obrigatório", ex.getMessage());
    }

    @Test
    @DisplayName("Estado anterior nulo só quando novo = ABERTA")
    void anteriorNuloApenasParaAberta() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                historicoSolicitacaoService.registrar(solicitacao, null, EstadoSolicitacao.ORCADA, funcionarioOrigem, null, null));
        assertEquals("Estado anterior nulo só é permitido para criação no estado ABERTA", ex.getMessage());
    }

    @Test
    @DisplayName("Destino informado fora de REDIRECIONADA falha")
    void destinoForaDeRedirecionadaFalha() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                historicoSolicitacaoService.registrar(solicitacao, EstadoSolicitacao.ABERTA, EstadoSolicitacao.ORCADA,
                        funcionarioOrigem, funcionarioDestino, null));
        assertEquals("Funcionário destino só deve ser informado quando o estado for REDIRECIONADA", ex.getMessage());
    }

    @Test
    @DisplayName("Redirecionamento exige funcionário destino")
    void redirecionamentoExigeDestino() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                historicoSolicitacaoService.registrar(solicitacao, EstadoSolicitacao.APROVADA, EstadoSolicitacao.REDIRECIONADA,
                        funcionarioOrigem, null, null));
        assertEquals("Funcionário destino é obrigatório no redirecionamento", ex.getMessage());
    }

    @Test
    @DisplayName("RF015: Redirecionar para si mesmo falha")
    void redirecionarParaSiMesmoFalha() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                historicoSolicitacaoService.registrar(solicitacao, EstadoSolicitacao.APROVADA, EstadoSolicitacao.REDIRECIONADA,
                        funcionarioOrigem, funcionarioOrigem, null));
        assertEquals("Funcionário destino não pode ser o mesmo funcionário responsável", ex.getMessage());
    }

    @Test
    @DisplayName("Destino com perfil CLIENTE falha")
    void destinoClienteFalha() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                historicoSolicitacaoService.registrar(solicitacao, EstadoSolicitacao.APROVADA, EstadoSolicitacao.REDIRECIONADA,
                        funcionarioOrigem, usuarioCliente, null));
        assertEquals("Funcionário destino deve ser do perfil FUNCIONARIO", ex.getMessage());
    }

    @Test
    @DisplayName("Destino inativo falha")
    void destinoInativoFalha() {
        funcionarioDestino.setAtivo(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                historicoSolicitacaoService.registrar(solicitacao, EstadoSolicitacao.APROVADA, EstadoSolicitacao.REDIRECIONADA,
                        funcionarioOrigem, funcionarioDestino, null));
        assertEquals("Funcionário destino deve estar ativo", ex.getMessage());
    }

    @Test
    @DisplayName("REJEITADA sem motivo ou com motivo em branco falha")
    void rejeitadaSemMotivoFalha() {
        IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class, () ->
                historicoSolicitacaoService.registrar(solicitacao, EstadoSolicitacao.ORCADA, EstadoSolicitacao.REJEITADA,
                        usuarioCliente, null, null));
        assertEquals("O motivo da rejeição é obrigatório", exNull.getMessage());

        IllegalArgumentException exBlank = assertThrows(IllegalArgumentException.class, () ->
                historicoSolicitacaoService.registrar(solicitacao, EstadoSolicitacao.ORCADA, EstadoSolicitacao.REJEITADA,
                        usuarioCliente, null, "    "));
        assertEquals("O motivo da rejeição é obrigatório", exBlank.getMessage());
    }

    @Test
    @DisplayName("REJEITADA com motivo grava observação com sucesso")
    void rejeitadaComMotivoGravaComSucesso() {
        when(historicoSolicitacaoRepository.save(any(HistoricoSolicitacao.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        HistoricoSolicitacao salvo = historicoSolicitacaoService.registrar(solicitacao, EstadoSolicitacao.ORCADA,
                EstadoSolicitacao.REJEITADA, usuarioCliente, null, "Achei caro");

        assertNotNull(salvo);
        assertEquals(EstadoSolicitacao.ORCADA, salvo.getEstadoAnterior());
        assertEquals(EstadoSolicitacao.REJEITADA, salvo.getEstado());
        assertEquals("Achei caro", salvo.getObservacao());
        assertEquals(usuarioCliente, salvo.getUsuario());
        assertNull(salvo.getUsuarioDestino());
    }

    @Test
    @DisplayName("Redirecionamento válido grava destino com sucesso")
    void redirecionamentoValidoGravaComSucesso() {
        when(historicoSolicitacaoRepository.save(any(HistoricoSolicitacao.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        HistoricoSolicitacao salvo = historicoSolicitacaoService.registrar(solicitacao, EstadoSolicitacao.APROVADA,
                EstadoSolicitacao.REDIRECIONADA, funcionarioOrigem, funcionarioDestino, null);

        assertNotNull(salvo);
        assertEquals(EstadoSolicitacao.APROVADA, salvo.getEstadoAnterior());
        assertEquals(EstadoSolicitacao.REDIRECIONADA, salvo.getEstado());
        assertEquals(funcionarioOrigem, salvo.getUsuario());
        assertEquals(funcionarioDestino, salvo.getUsuarioDestino());
    }
}

