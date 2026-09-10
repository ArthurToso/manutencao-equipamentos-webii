import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface EnderecoPayload {
  cep: string;
  logradouro: string;
  numero: string;
  complemento?: string;
  bairro: string;
  cidade: string;
  uf: string;
}

export interface AutocadastroPayload {
  cpf: string;
  email: string;
  nome: string;
  telefone: string;
  endereco: EnderecoPayload;
}

@Injectable({ providedIn: 'root' })
export class AutocadastroService {

  private http = inject(HttpClient)

  // TODO: migrar para environment.ts (decidido com a equipe)
  private readonly baseUrl = 'http://localhost:8080/api'

  // TODO: confirmar contrato com o backend
  // - path do endpoint
  // - se o corpo vai com endereco aninhado ou plano
  // - o que a resposta devolve (hoje assumido 201 sem corpo)
  cadastrar(dados: AutocadastroPayload): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/clientes`, dados)
  }
}