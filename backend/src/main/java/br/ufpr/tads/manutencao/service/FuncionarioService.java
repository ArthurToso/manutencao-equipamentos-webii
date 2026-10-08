package br.ufpr.tads.manutencao.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.ufpr.tads.manutencao.dto.FuncionarioRequest;
import br.ufpr.tads.manutencao.dto.FuncionarioResponse;
import br.ufpr.tads.manutencao.model.Funcionario;
import br.ufpr.tads.manutencao.model.Perfil;
import br.ufpr.tads.manutencao.model.Usuario;
import br.ufpr.tads.manutencao.repository.FuncionarioRepository;
import br.ufpr.tads.manutencao.repository.UsuarioRepository;
import br.ufpr.tads.manutencao.util.SenhaUtils;
import jakarta.transaction.Transactional;

@Service 
public class FuncionarioService {
    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioRepository usuarioRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository, UsuarioRepository usuarioRepository){
        this.funcionarioRepository = funcionarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    private FuncionarioResponse toResponse(Funcionario funcionario) {
        Usuario usuario = funcionario.getUsuario();
        return new FuncionarioResponse(
                funcionario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                funcionario.getDataNascimento());
    }

    public List<FuncionarioResponse> listar() {
        return funcionarioRepository.findByUsuarioAtivoTrueOrderByUsuarioNomeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FuncionarioResponse buscarPorId(Long id) {
        return toResponse(buscarAtivo(id));
    }

    private Funcionario buscarAtivo(Long id) {
        return funcionarioRepository.findByIdAndUsuarioAtivoTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));
    }

    @Transactional
    public FuncionarioResponse criar(FuncionarioRequest request) {
        String senha = request.getSenha();
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("A senha é obrigatória no cadastro");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema");
        }

        String salt = SenhaUtils.gerarSalt();
        String senhaHash = SenhaUtils.gerarHashSha256ComSalt(senha, salt);

        Usuario usuario = new Usuario(request.getNome().trim(), email, senhaHash, salt, Perfil.FUNCIONARIO);
        Funcionario funcionario = new Funcionario(usuario, request.getDataNascimento());

        Funcionario salvo = funcionarioRepository.save(funcionario);
        return toResponse(salvo);
    }

    @Transactional
    public FuncionarioResponse atualizar(Long id, FuncionarioRequest request) {
        Funcionario funcionario = buscarAtivo(id);
        Usuario usuario = funcionario.getUsuario();

        String email = request.getEmail().trim().toLowerCase();
        Optional<Usuario> donoDoEmail = usuarioRepository.findByEmailIgnoreCase(email);
        if (donoDoEmail.isPresent() && !donoDoEmail.get().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema");
        }

        usuario.setNome(request.getNome().trim());
        usuario.setEmail(email);
        funcionario.setDataNascimento(request.getDataNascimento());

        String senha = request.getSenha();
        if (senha != null && !senha.isBlank()) {
            String salt = SenhaUtils.gerarSalt();
            usuario.setSalt(salt);
            usuario.setSenha(SenhaUtils.gerarHashSha256ComSalt(senha, salt));
        }

        return toResponse(funcionarioRepository.save(funcionario));
    }

    @Transactional
    public void remover(Long id) {
        Funcionario funcionario = buscarAtivo(id);

        if (funcionarioRepository.countByUsuarioAtivoTrue() <= 1) {
            throw new IllegalStateException("Não é possível remover o único funcionário ativo");
        }

        // TODO: impedir que o funcionário remova a si mesmo (depende do login, issue #16)

        funcionario.getUsuario().setAtivo(false);
        funcionarioRepository.save(funcionario);
    }

}
