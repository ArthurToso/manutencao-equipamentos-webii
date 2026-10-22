package br.ufpr.tads.manutencao.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.ufpr.tads.manutencao.dto.FuncionarioDestinoDTO;
import br.ufpr.tads.manutencao.dto.HistoricoDTO;
import br.ufpr.tads.manutencao.dto.SolicitacaoResponse;
import br.ufpr.tads.manutencao.dto.UsuarioResumoDTO;
import br.ufpr.tads.manutencao.model.HistoricoSolicitacao;
import br.ufpr.tads.manutencao.model.Solicitacao;

@Component
public class SolicitacaoMapper {

    public SolicitacaoResponse toResponse(Solicitacao solicitacao) {
        if (solicitacao == null) {
            return null;
        }
        SolicitacaoResponse response = new SolicitacaoResponse();
        response.setId(solicitacao.getId());
        response.setDescricaoEquipamento(solicitacao.getDescricaoEquipamento());
        if (solicitacao.getCategoria() != null) {
            response.setCategoriaId(solicitacao.getCategoria().getId());
            response.setCategoriaNome(solicitacao.getCategoria().getNome());
        }
        response.setDescricaoDefeito(solicitacao.getDescricaoDefeito());
        response.setDataHora(solicitacao.getDataHora());
        if (solicitacao.getEstado() != null) {
            response.setEstado(solicitacao.getEstado().name());
        }
        response.setClienteId(solicitacao.getClienteId());
        response.setValorOrcamento(solicitacao.getValorOrcamento());
        response.setMotivoRejeicao(solicitacao.getMotivoRejeicao());
        return response;
    }

    public SolicitacaoResponse toResponseComHistorico(Solicitacao solicitacao, List<HistoricoSolicitacao> historicos) {
        SolicitacaoResponse response = toResponse(solicitacao);
        if (response != null) {
            if (historicos != null && !historicos.isEmpty()) {
                response.setHistorico(historicos.stream()
                        .map(this::toHistoricoDTO)
                        .collect(Collectors.toList()));
            } else {
                response.setHistorico(Collections.emptyList());
            }
        }
        return response;
    }

    public HistoricoDTO toHistoricoDTO(HistoricoSolicitacao h) {
        if (h == null) {
            return null;
        }
        HistoricoDTO dto = new HistoricoDTO();
        dto.setId(h.getId());
        dto.setEstadoAnterior(h.getEstadoAnterior() != null ? h.getEstadoAnterior().name() : null);
        dto.setEstado(h.getEstado() != null ? h.getEstado().name() : null);
        dto.setDataHora(h.getDataHora());
        dto.setObservacao(h.getObservacao());

        if (h.getUsuario() != null) {
            dto.setNomeFuncionario(h.getUsuario().getNome());
            dto.setResponsavel(new UsuarioResumoDTO(
                    h.getUsuario().getId(),
                    h.getUsuario().getNome(),
                    h.getUsuario().getPerfil() != null ? h.getUsuario().getPerfil().name() : null
            ));
        }

        if (h.getUsuarioDestino() != null) {
            dto.setFuncionarioDestino(new FuncionarioDestinoDTO(
                    h.getUsuarioDestino().getId(),
                    h.getUsuarioDestino().getNome()
            ));
        }

        return dto;
    }
}

