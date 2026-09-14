import { EstadoSolicitacao } from "./solicitacao.model"

export interface HistoricoSolicitacao {
    id: number
    estadoDestino: EstadoSolicitacao
    dataHora: Date
    responsavel: string 
}
