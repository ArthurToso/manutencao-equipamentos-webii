import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ReceitaDiaDTO {
  data: string;
  receita: number;
}

export interface ReceitaCategoriaDTO {
  categoria: string;
  receita: number;
}

@Injectable({
  providedIn: 'root'
})
export class RelatorioService {

  private readonly API = 'http://localhost:8080/api/relatorios';

  constructor(private http: HttpClient) { }

  buscarReceitasPorDia(dataInicial?: string, dataFinal?: string): Observable<ReceitaDiaDTO[]> {
    let params = new HttpParams();
    if (dataInicial) {
      params = params.set('dataInicial', dataInicial);
    }
    if (dataFinal) {
      params = params.set('dataFinal', dataFinal);
    }
    return this.http.get<ReceitaDiaDTO[]>(`${this.API}/receitas`, { params });
  }

  buscarReceitasPorCategoria(): Observable<ReceitaCategoriaDTO[]> {
    return this.http.get<ReceitaCategoriaDTO[]>(`${this.API}/receitas-categoria`);
  }
}
