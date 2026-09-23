import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

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

  constructor() { }

  buscarReceitasPorDia(dataInicial?: string, dataFinal?: string): Observable<ReceitaDiaDTO[]> {
    // Mock data for prototype
    return of([
      { data: '2026-09-20', receita: 1500.00 },
      { data: '2026-09-21', receita: 320.50 },
      { data: '2026-09-22', receita: 450.00 }
    ]);
  }

  buscarReceitasPorCategoria(): Observable<ReceitaCategoriaDTO[]> {
    // Mock data for prototype
    return of([
      { categoria: 'Notebook', receita: 2500.00 },
      { categoria: 'Desktop', receita: 1200.00 },
      { categoria: 'Impressora', receita: 450.00 },
      { categoria: 'Teclado', receita: 120.00 }
    ]);
  }
}
