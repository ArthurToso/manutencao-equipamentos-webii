import { HttpClient } from '@angular/common/http';
import { inject, Injectable} from '@angular/core';
import { Observable } from 'rxjs';
import { FuncionarioDTO, FuncionarioCrudDTO } from '../shared';

@Injectable({providedIn: 'root'})
export class FuncionarioService {
    private http = inject(HttpClient)
    private readonly baseUrl = "http://localhost:8080/api/funcionarios"

    public listar(): Observable<FuncionarioDTO[]>{
        return this.http.get<FuncionarioDTO[]>(this.baseUrl)
    }

    public remover(id: number): Observable<void>{
        return this.http.delete<void>(`${this.baseUrl}/${id}`)
    }

    public buscarPorId(id: number): Observable<FuncionarioDTO>{
        return this.http.get<FuncionarioDTO>(`${this.baseUrl}/${id}`)
    }

    public criar(dados: FuncionarioCrudDTO): Observable<FuncionarioDTO>{
        return this.http.post<FuncionarioDTO>(this.baseUrl, dados)
    }

    public atualizar(id: number, dados: FuncionarioCrudDTO): Observable<FuncionarioDTO>{
        return this.http.put<FuncionarioDTO>(`${this.baseUrl}/${id}`, dados)
    }

}

