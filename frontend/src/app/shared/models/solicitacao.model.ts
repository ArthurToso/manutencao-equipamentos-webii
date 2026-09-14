import { Categoria } from "./categoria.model"
import { HistoricoSolicitacao } from "./historico-solicitacao.model"
import { Usuario } from "./usuario.model"

export type EstadoSolicitacao = 'ABERTA' | 'ORÇADA' | 'APROVADA' | 'REJEITADA' | 'ARRUMADA' | 'PAGA' | 'FINALIZADA' | 'REDIRECIONADA'

export interface Solicitacao {
    id: number
    equipamento: string
    categoria: Categoria
    defeito: string
    dataHora: Date
    estado: EstadoSolicitacao
    historico: HistoricoSolicitacao[]
    valorOrcamento?: number
    descricaoManutencao?: string
    orientacoesCliente?: string
    cliente: Usuario
}
