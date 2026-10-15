import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Categoria } from '../shared';

@Injectable({
  providedIn: 'root'
})
export class CategoriaService {

  private readonly API = 'http://localhost:8080/api/categorias';

  constructor(private http: HttpClient) { }

  listar(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.API);
  }

  criar(nome: string): Observable<Categoria> {
    return this.http.post<Categoria>(this.API, { nome });
  }

  atualizar(id: number, nome: string): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.API}/${id}`, { nome });
  }

  remover(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }
}
