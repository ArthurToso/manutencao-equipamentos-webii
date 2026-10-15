package br.ufpr.tads.manutencao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import br.ufpr.tads.manutencao.model.Funcionario;
import br.ufpr.tads.manutencao.model.Perfil;
import br.ufpr.tads.manutencao.model.Usuario;
import br.ufpr.tads.manutencao.repository.FuncionarioRepository;
import br.ufpr.tads.manutencao.repository.UsuarioRepository;
import br.ufpr.tads.manutencao.security.UsuarioAutenticado;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private FuncionarioService funcionarioService;

    @BeforeEach
    void setUp() {
        // Usuário logado: Maria (usuarioId=1, funcionarioId=10)
        UsuarioAutenticado mariaAutenticada = new UsuarioAutenticado(
                1L, "maria@example.com", "FUNCIONARIO", null, 10L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mariaAutenticada, null, mariaAutenticada.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Funcionário não pode remover a si mesmo (RF018)")
    void deveImpedirAutoRemocao() {
        Usuario usuarioMaria = new Usuario("Maria", "maria@example.com", "hash", "salt", Perfil.FUNCIONARIO);
        usuarioMaria.setId(1L);
        Funcionario maria = new Funcionario(usuarioMaria, LocalDate.of(1990, 1, 1));
        maria.setId(10L);

        when(funcionarioRepository.findByIdAndUsuarioAtivoTrue(10L)).thenReturn(Optional.of(maria));
        when(funcionarioRepository.countByUsuarioAtivoTrue()).thenReturn(3L);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                funcionarioService.remover(10L)
        );

        assertEquals("Não é possível remover o próprio usuário conectado", ex.getMessage());
    }

    @Test
    @DisplayName("Não é possível remover o único funcionário ativo")
    void deveImpedirRemoverUnicoFuncionarioAtivo() {
        Usuario outroUsuario = new Usuario("Pedro", "pedro@example.com", "hash", "salt", Perfil.FUNCIONARIO);
        outroUsuario.setId(2L);
        Funcionario pedro = new Funcionario(outroUsuario, LocalDate.of(1992, 5, 10));
        pedro.setId(11L);

        when(funcionarioRepository.findByIdAndUsuarioAtivoTrue(11L)).thenReturn(Optional.of(pedro));
        when(funcionarioRepository.countByUsuarioAtivoTrue()).thenReturn(1L);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                funcionarioService.remover(11L)
        );

        assertEquals("Não é possível remover o único funcionário ativo", ex.getMessage());
    }

    @Test
    @DisplayName("Funcionário pode remover outro funcionário se houver mais de um ativo")
    void deveRemoverOutroFuncionarioComSucesso() {
        Usuario outroUsuario = new Usuario("Pedro", "pedro@example.com", "hash", "salt", Perfil.FUNCIONARIO);
        outroUsuario.setId(2L);
        outroUsuario.setAtivo(true);
        Funcionario pedro = new Funcionario(outroUsuario, LocalDate.of(1992, 5, 10));
        pedro.setId(11L);

        when(funcionarioRepository.findByIdAndUsuarioAtivoTrue(11L)).thenReturn(Optional.of(pedro));
        when(funcionarioRepository.countByUsuarioAtivoTrue()).thenReturn(3L);

        funcionarioService.remover(11L);

        assertFalse(outroUsuario.isAtivo());
        verify(funcionarioRepository).save(pedro);
    }
}

