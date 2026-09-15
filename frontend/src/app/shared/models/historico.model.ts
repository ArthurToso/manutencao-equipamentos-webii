import { EstadoSolicitacao } from "../enums";
import { UsuarioDTO } from './';

export interface HistoricoGenerico {
    id: number;
    dataHora: Date;
    userResponsavel: UsuarioDTO;
    estado: EstadoSolicitacao;
}

export interface HistoricoOrcamento extends HistoricoGenerico {
    estado: EstadoSolicitacao.ORCADA;
    valor: number;
}

export interface HistoricoRejeicao extends HistoricoGenerico {
    estado: EstadoSolicitacao.REJEITADA;
    motivo: string;
}

export interface HistoricoManutencao extends HistoricoGenerico {
    estado: EstadoSolicitacao.ARRUMADA;
    descricao: string;
    orientacoes: string;
}

export interface HistoricoRedirecionamento extends HistoricoGenerico {
    estado: EstadoSolicitacao.REDIRECIONADA;
    funcionarioDestino: UsuarioDTO;
}

export type Historico = 
    | HistoricoGenerico
    | HistoricoManutencao
    | HistoricoOrcamento
    | HistoricoRedirecionamento
    | HistoricoRejeicao;