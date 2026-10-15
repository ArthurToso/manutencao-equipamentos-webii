package br.ufpr.tads.manutencao.service;

import org.springframework.stereotype.Service;

import br.ufpr.tads.manutencao.dto.LoginRequest;
import br.ufpr.tads.manutencao.dto.LoginResponse;
import br.ufpr.tads.manutencao.dto.UsuarioAuthDTO;
import br.ufpr.tads.manutencao.exception.CredenciaisInvalidasException;
import br.ufpr.tads.manutencao.model.Cliente;
import br.ufpr.tads.manutencao.model.Funcionario;
import br.ufpr.tads.manutencao.model.Perfil;
import br.ufpr.tads.manutencao.model.Usuario;
import br.ufpr.tads.manutencao.repository.ClienteRepository;
import br.ufpr.tads.manutencao.repository.FuncionarioRepository;
import br.ufpr.tads.manutencao.repository.UsuarioRepository;
import br.ufpr.tads.manutencao.security.JwtService;
import br.ufpr.tads.manutencao.util.SenhaUtils;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            FuncionarioRepository funcionarioRepository,
            JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!usuario.isAtivo()) {
            throw new CredenciaisInvalidasException();
        }

        boolean senhaCorreta = SenhaUtils.verificarSenha(
                request.getSenha(),
                usuario.getSalt(),
                usuario.getSenha()
        );

        if (!senhaCorreta) {
            throw new CredenciaisInvalidasException();
        }

        Long clienteId = null;
        Long funcionarioId = null;

        if (usuario.getPerfil() == Perfil.CLIENTE) {
            clienteId = clienteRepository.findByUsuarioId(usuario.getId())
                    .map(Cliente::getId)
                    .orElse(null);
        } else if (usuario.getPerfil() == Perfil.FUNCIONARIO) {
            funcionarioId = funcionarioRepository.findByUsuarioId(usuario.getId())
                    .map(Funcionario::getId)
                    .orElse(null);
        }

        String token = jwtService.gerarToken(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getPerfil().name(),
                clienteId,
                funcionarioId
        );

        UsuarioAuthDTO usuarioDTO = new UsuarioAuthDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().name(),
                clienteId,
                funcionarioId
        );

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                usuarioDTO
        );
    }
}

