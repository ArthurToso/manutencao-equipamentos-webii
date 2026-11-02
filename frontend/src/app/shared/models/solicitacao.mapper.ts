import { EstadoSolicitacao } from '../enums';

export function mapearEstadoSolicitacao(estadoStr: string | null | undefined): EstadoSolicitacao {
  if (!estadoStr) {
    throw new Error('Estado da solicitação não informado');
  }

  const normalizado = estadoStr.toUpperCase().trim();

  switch (normalizado) {
    case 'ABERTA':
      return EstadoSolicitacao.ABERTA;
    case 'ORCADA':
    case 'ORÇADA':
      return EstadoSolicitacao.ORCADA;
    case 'APROVADA':
      return EstadoSolicitacao.APROVADA;
    case 'REJEITADA':
      return EstadoSolicitacao.REJEITADA;
    case 'REDIRECIONADA':
      return EstadoSolicitacao.REDIRECIONADA;
    case 'ARRUMADA':
      return EstadoSolicitacao.ARRUMADA;
    case 'PAGA':
      return EstadoSolicitacao.PAGA;
    case 'FINALIZADA':
      return EstadoSolicitacao.FINALIZADA;
    default:
      console.error(`Estado de solicitação desconhecido retornado pela API: ${estadoStr}`);
      throw new Error(`Estado de solicitação desconhecido: ${estadoStr}`);
  }
}

