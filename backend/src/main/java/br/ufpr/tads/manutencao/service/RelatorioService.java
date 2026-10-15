package br.ufpr.tads.manutencao.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.ufpr.tads.manutencao.dto.ReceitaCategoriaResponse;
import br.ufpr.tads.manutencao.dto.ReceitaDiaResponse;
import br.ufpr.tads.manutencao.repository.HistoricoSolicitacaoRepository;

@Service
public class RelatorioService {

    private final HistoricoSolicitacaoRepository historicoRepository;

    public RelatorioService(HistoricoSolicitacaoRepository historicoRepository) {
        this.historicoRepository = historicoRepository;
    }

    public List<ReceitaDiaResponse> receitasPorDia(LocalDate dataInicial, LocalDate dataFinal) {
        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
            throw new IllegalArgumentException("A data inicial não pode ser maior que a data final");
        }

        LocalDateTime inicio = dataInicial != null ? dataInicial.atStartOfDay() : LocalDateTime.of(1900, 1, 1, 0, 0);
        LocalDateTime fim = dataFinal != null ? dataFinal.plusDays(1).atStartOfDay() : LocalDateTime.of(9999, 12, 31, 0, 0);

        return historicoRepository.somarReceitasPorDia(inicio, fim);
    }

    public List<ReceitaCategoriaResponse> receitasPorCategoria() {
        return historicoRepository.somarReceitasPorCategoria();
    }
}
