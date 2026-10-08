import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SolicitacaoDetailedDTO } from '../shared';

@Injectable({
  providedIn: 'root'
})
export class SolicitacaoService {

  private readonly API = 'http://localhost:8080/api/solicitacoes';

  constructor(private http: HttpClient) { }

  buscarPorId(id: number): Observable<any> {
    return this.http.get<any>(`${this.API}/${id}`);
  }
}
