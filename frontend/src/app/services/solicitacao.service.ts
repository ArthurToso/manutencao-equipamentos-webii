import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NovaSolicitacaoDTO, SolicitacaoApiDTO, SolicitacaoCardDTO, SolicitacaoCardFuncDTO } from '../shared';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SolicitacaoService {

  private readonly API = `${environment.apiUrl}/solicitacoes`;

  constructor(private http: HttpClient) { }

  buscarPorId(id: number): Observable<any> {
    return this.http.get<any>(`${this.API}/${id}`);
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
}
