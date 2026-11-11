import { ChangeDetectorRef, inject, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RelatorioService, ReceitaCategoriaDTO } from '../../../services/relatorio.service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-funcionario-relatorio-categorias',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './funcionario-relatorio-categorias.html',
  styleUrls: ['./funcionario-relatorio-categorias.css']
})
export class FuncionarioRelatorioCategorias implements OnInit {
  private cdr = inject(ChangeDetectorRef);
  receitas: ReceitaCategoriaDTO[] = [];
  totalReceita: number = 0;

  constructor(private relatorioService: RelatorioService) {}

  ngOnInit(): void {
    this.buscarReceitas();
  }

  buscarReceitas(): void {
    this.relatorioService.buscarReceitasPorCategoria().pipe(
      finalize(() => this.cdr.detectChanges())
    ).subscribe(data => {
      this.receitas = data;
      this.calcularTotal();
    });
  }

  calcularTotal(): void {
    this.totalReceita = this.receitas.reduce((acc, curr) => acc + curr.receita, 0);
  }

  imprimirPDF(): void {
    window.print();
  }
}
