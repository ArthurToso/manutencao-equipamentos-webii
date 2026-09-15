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