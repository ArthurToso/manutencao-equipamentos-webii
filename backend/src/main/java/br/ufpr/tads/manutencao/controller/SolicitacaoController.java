package br.ufpr.tads.manutencao.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ufpr.tads.manutencao.dto.OrcamentoRequest;
import br.ufpr.tads.manutencao.dto.RejeicaoRequest;
import br.ufpr.tads.manutencao.dto.SolicitacaoRequest;
import br.ufpr.tads.manutencao.dto.SolicitacaoResponse;
import br.ufpr.tads.manutencao.security.UsuarioAutenticado;
import br.ufpr.tads.manutencao.service.SolicitacaoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    public SolicitacaoController(SolicitacaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    @GetMapping("/abertas")
    public ResponseEntity<List<SolicitacaoResponse>> buscarAbertas(
            @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
        List<SolicitacaoResponse> response = solicitacaoService.buscarAbertas(usuarioAutenticado);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/orcamento")
    public ResponseEntity<SolicitacaoResponse> efetuarOrcamento(
            @PathVariable Long id,
            @Valid @RequestBody OrcamentoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
        SolicitacaoResponse response = solicitacaoService.efetuarOrcamento(id, request.getValor(), usuarioAutenticado);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SolicitacaoResponse> criar(
            @Valid @RequestBody SolicitacaoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
        SolicitacaoResponse response = solicitacaoService.criarSolicitacao(request, usuarioAutenticado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/minhas")
    public ResponseEntity<List<SolicitacaoResponse>> buscarMinhas(
            @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
        List<SolicitacaoResponse> response = solicitacaoService.buscarMinhas(usuarioAutenticado);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/orcamento")
    public ResponseEntity<SolicitacaoResponse> buscarOrcamento(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
        SolicitacaoResponse response = solicitacaoService.buscarOrcamento(id, usuarioAutenticado);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoResponse> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
        SolicitacaoResponse response = solicitacaoService.buscarPorId(id, usuarioAutenticado);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/aprovar")
    public ResponseEntity<SolicitacaoResponse> aprovarServico(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
        SolicitacaoResponse response = solicitacaoService.aprovarServico(id, usuarioAutenticado);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/rejeitar")
    public ResponseEntity<SolicitacaoResponse> rejeitarServico(
            @PathVariable Long id,
            @Valid @RequestBody RejeicaoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
        SolicitacaoResponse response = solicitacaoService.rejeitarServico(id, request.getMotivo(), usuarioAutenticado);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/resgatar")
    public ResponseEntity<SolicitacaoResponse> resgatarServico(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
        SolicitacaoResponse response = solicitacaoService.resgatarServico(id, usuarioAutenticado);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<SolicitacaoResponse> pagarServico(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
        SolicitacaoResponse response = solicitacaoService.pagarServico(id, usuarioAutenticado);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<SolicitacaoResponse> finalizarSolicitacao(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
        SolicitacaoResponse response = solicitacaoService.finalizarSolicitacao(id, usuarioAutenticado);
        return ResponseEntity.ok(response);
    }
}
