package br.ufpr.tads.manutencao.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.ufpr.tads.manutencao.dto.ReceitaCategoriaResponse;
import br.ufpr.tads.manutencao.dto.ReceitaDiaResponse;
import br.ufpr.tads.manutencao.service.RelatorioService;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/receitas")
    public ResponseEntity<List<ReceitaDiaResponse>> receitasPorDia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {
        return ResponseEntity.ok(relatorioService.receitasPorDia(dataInicial, dataFinal));
    }

    @GetMapping("/receitas-categoria")
    public ResponseEntity<List<ReceitaCategoriaResponse>> receitasPorCategoria() {
        return ResponseEntity.ok(relatorioService.receitasPorCategoria());
    }
}
