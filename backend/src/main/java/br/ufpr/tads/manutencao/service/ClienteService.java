package br.ufpr.tads.manutencao.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufpr.tads.manutencao.dto.AutocadastroClienteDTO;
import br.ufpr.tads.manutencao.dto.ClienteResponseDTO;
import br.ufpr.tads.manutencao.dto.EnderecoDTO;
import br.ufpr.tads.manutencao.model.Cliente;
import br.ufpr.tads.manutencao.model.Endereco;
import br.ufpr.tads.manutencao.model.Perfil;
import br.ufpr.tads.manutencao.model.Usuario;
import br.ufpr.tads.manutencao.repository.ClienteRepository;
import br.ufpr.tads.manutencao.repository.UsuarioRepository;
import br.ufpr.tads.manutencao.util.CpfUtils;
import br.ufpr.tads.manutencao.util.SenhaUtils;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    public ClienteService(ClienteRepository clienteRepository,
                          UsuarioRepository usuarioRepository,
                          EmailService emailService) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    @Transactional
    public ClienteResponseDTO autocadastro(AutocadastroClienteDTO dto) {
        // 1. Validação de formato e dígitos verificadores do CPF
        String cpfLimpo = CpfUtils.limpar(dto.getCpf());
        if (!CpfUtils.isValido(cpfLimpo)) {
            throw new IllegalArgumentException("CPF informado é inválido");
        }

        // 2. Validação de unicidade de CPF
        if (clienteRepository.existsByCpf(cpfLimpo)) {
            throw new IllegalArgumentException("CPF já cadastrado no sistema");
        }

        // 3. Validação de unicidade de E-mail (login)
        String emailFormatado = dto.getEmail().trim().toLowerCase();
        if (clienteRepository.existsByEmailIgnoreCase(emailFormatado) ||
            usuarioRepository.existsByEmailIgnoreCase(emailFormatado)) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema");
        }

        // 4. Geração automática de senha aleatória de 4 números
        String senhaTemporaria = SenhaUtils.gerarSenhaAleatoria();

        // 5. Criptografia: Hash SHA-256 + Salt
        String salt = SenhaUtils.gerarSalt();
        String senhaHash = SenhaUtils.gerarHashSha256ComSalt(senhaTemporaria, salt);

        // 6. Criação do Usuario correspondente (Login = E-mail, Perfil = CLIENTE)
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(emailFormatado);
        usuario.setSenha(senhaHash);
        usuario.setSalt(salt);
        usuario.setPerfil(Perfil.CLIENTE);
        usuario.setAtivo(true);

        // 7. Criação do Endereço completo
        EnderecoDTO endDTO = dto.getEndereco();
        Endereco endereco = new Endereco();
        endereco.setCep(endDTO.getCep().replaceAll("\\D", ""));
        endereco.setLogradouro(endDTO.getLogradouro().trim());
        endereco.setNumero(String.valueOf(endDTO.getNumero()).trim());
        endereco.setComplemento(endDTO.getComplemento() != null && !endDTO.getComplemento().isBlank()
                ? endDTO.getComplemento().trim() : null);
        endereco.setBairro(endDTO.getBairro().trim());
        endereco.setCidade(endDTO.getCidade().trim());
        endereco.setUf(endDTO.getUf().trim().toUpperCase());

        // 8. Criação do Cliente
        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome().trim());
        cliente.setCpf(cpfLimpo);
        cliente.setEmail(emailFormatado);
        cliente.setTelefone(dto.getTelefone().trim());
        cliente.setUsuario(usuario);
        cliente.setEndereco(endereco);

        Cliente clienteSalvo = clienteRepository.save(cliente);

        // 9. Notificação: Enviar senha temporária por e-mail no ato do cadastro
        emailService.enviarSenhaTemporaria(emailFormatado, clienteSalvo.getNome(), senhaTemporaria);

        return toResponse(clienteSalvo);
    }

    private ClienteResponseDTO toResponse(Cliente cliente) {
        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setId(cliente.getId());
        response.setNome(cliente.getNome());
        response.setCpf(cliente.getCpf());
        response.setEmail(cliente.getEmail());
        response.setTelefone(cliente.getTelefone());
        response.setPerfil(Perfil.CLIENTE.name());
        response.setMensagem("Autocadastro realizado com sucesso! Uma senha temporária de 4 dígitos foi enviada para o seu e-mail.");

        if (cliente.getEndereco() != null) {
            Endereco endereco = cliente.getEndereco();
            EnderecoDTO enderecoDTO = new EnderecoDTO();
            enderecoDTO.setCep(endereco.getCep());
            enderecoDTO.setLogradouro(endereco.getLogradouro());
            enderecoDTO.setNumero(endereco.getNumero());
            enderecoDTO.setComplemento(endereco.getComplemento());
            enderecoDTO.setBairro(endereco.getBairro());
            enderecoDTO.setCidade(endereco.getCidade());
            enderecoDTO.setUf(endereco.getUf());
            response.setEndereco(enderecoDTO);
        }

        return response;
    }
}
