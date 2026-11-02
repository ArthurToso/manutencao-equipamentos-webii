import { Categoria } from "./categoria.model";
import { EstadoSolicitacao } from "../enums";
import { ClienteDTO } from "./usuario.model";
import { Historico } from "./historico.model";

export interface SolicitacaoCardDTO {
    id: number;
    dataHora: Date;
    descricaoEquipamento: string;
    estado: EstadoSolicitacao;
}

export interface SolicitacaoCardFuncDTO extends SolicitacaoCardDTO {
    nomeCliente: string;
}

export interface SolicitacaoDetailedDTO extends SolicitacaoCardDTO {
    descricaoProblema: string;
    categoriaEquipamento: Categoria;
    historico: Historico[]; 
    
    valorOrcado?: number;
    orientacoesCliente?: string;
    descricaoManutencao?: string;
    motivoRejeicao?: string;
}

export interface SolicitacaoDetailedFuncDTO extends SolicitacaoDetailedDTO {
    cliente: ClienteDTO;
}

export interface NovaSolicitacaoDTO {
    descricaoEquipamento: string;
    categoriaId: number;
    descricaoDefeito: string;
}

export interface HistoricoApiDTO {
    id?: number;
    estadoAnterior?: string | null;
    estado?: string;
    dataHora?: string;
    observacao?: string | null;
    nomeFuncionario?: string | null;
}

export interface SolicitacaoApiDTO {
    id: number;
    descricaoEquipamento: string;
    categoriaId: number;
    categoriaNome: string;
    descricaoDefeito: string;
    dataHora: string;
    estado: string;
    clienteId: number;
    nomeCliente?: string | null;
    valorOrcamento?: number | null;
    motivoRejeicao?: string | null;
    cliente?: any;
    historico?: any[];
}
