package br.ufpr.tads.manutencao.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ufpr.tads.manutencao.dto.AutocadastroClienteDTO;
import br.ufpr.tads.manutencao.dto.ClienteResponseDTO;
import br.ufpr.tads.manutencao.service.ClienteService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class AutocadastroController {

    private final ClienteService clienteService;

    public AutocadastroController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping({"/autocadastro", "/clientes"})
    public ResponseEntity<ClienteResponseDTO> autocadastrar(@Valid @RequestBody AutocadastroClienteDTO request) {
        ClienteResponseDTO response = clienteService.autocadastro(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
