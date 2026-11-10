import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  EfetuarManutencaoDTO,
  NovaSolicitacaoDTO,
  RedirecionarSolicitacaoDTO,
  SolicitacaoApiDTO,
  SolicitacaoCardDTO,
  SolicitacaoCardFuncDTO,
  mapearEstadoSolicitacao
} from '../shared';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SolicitacaoService {

  private readonly API = `${environment.apiUrl}/solicitacoes`;

  constructor(private http: HttpClient) { }

  listarTodas(
    filtro: 'HOJE' | 'PERIODO' | 'TODAS',
    dataInicial?: string,
    dataFinal?: string
  ): Observable<SolicitacaoCardFuncDTO[]> {
    let params = new HttpParams().set('filtro', filtro);
    if (dataInicial) {
      params = params.set('dataInicial', dataInicial);
    }
    if (dataFinal) {
      params = params.set('dataFinal', dataFinal);
    }
    return this.http.get<SolicitacaoApiDTO[]>(this.API, { params }).pipe(
      map(lista =>
        lista.map(item => ({
          id: item.id,
          dataHora: new Date(item.dataHora.includes('T') ? item.dataHora : item.dataHora.replace(' ', 'T')),
          descricaoEquipamento: item.descricaoEquipamento,
          estado: mapearEstadoSolicitacao(item.estado),
          nomeCliente: item.nomeCliente || item.cliente?.nome || ''
        }))
      )
    );
  }

  buscarPorId(id: number): Observable<SolicitacaoApiDTO> {
    return this.http.get<SolicitacaoApiDTO>(`${this.API}/${id}`);
  }

  buscarAbertas(): Observable<SolicitacaoCardFuncDTO[]> {
    return this.http.get<SolicitacaoCardFuncDTO[]>(`${this.API}/abertas`);
  }

  efetuarOrcamento(id: number, valor: number): Observable<any> {
    return this.http.post<any>(`${this.API}/${id}/orcamento`, { valor });
  }

  listarMinhas(): Observable<SolicitacaoCardDTO[]> {
    return this.http.get<SolicitacaoCardDTO[]>(`${this.API}/minhas`);
  }

  criar(dados: NovaSolicitacaoDTO): Observable<SolicitacaoCardDTO> {
    return this.http.post<SolicitacaoCardDTO>(this.API, dados);
  }

  buscarOrcamento(id: number): Observable<SolicitacaoApiDTO> {
    return this.http.get<SolicitacaoApiDTO>(`${this.API}/${id}/orcamento`);
  }

  aprovar(id: number): Observable<SolicitacaoApiDTO> {
    return this.http.put<SolicitacaoApiDTO>(`${this.API}/${id}/aprovar`, null);
  }

  rejeitar(id: number, motivo: string): Observable<SolicitacaoApiDTO> {
    return this.http.put<SolicitacaoApiDTO>(`${this.API}/${id}/rejeitar`, { motivo });
  }

  resgatarServico(id: number): Observable<any> {
    return this.http.put<any>(`${this.API}/${id}/resgatar`, {});
  }

  pagarServico(id: number): Observable<any> {
    return this.http.put<any>(`${this.API}/${id}/pagar`, {});
  }

  finalizarServico(id: number): Observable<SolicitacaoCardDTO> {
    return this.http.put<SolicitacaoCardDTO>(`${this.API}/${id}/finalizar`, {});
  }

  efetuarManutencao(
    id: number,
    dados: EfetuarManutencaoDTO
  ): Observable<SolicitacaoApiDTO> {
    return this.http.put<SolicitacaoApiDTO>(`${this.API}/${id}/manutencao`, dados);
  }

  redirecionar(
    id: number,
    dados: RedirecionarSolicitacaoDTO
  ): Observable<SolicitacaoApiDTO> {
    return this.http.put<SolicitacaoApiDTO>(`${this.API}/${id}/redirecionar`, dados);
  }
}
