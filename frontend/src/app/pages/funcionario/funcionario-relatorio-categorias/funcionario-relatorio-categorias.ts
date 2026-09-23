import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RelatorioService, ReceitaCategoriaDTO } from '../../../services/relatorio.service';

@Component({
  selector: 'app-funcionario-relatorio-categorias',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './funcionario-relatorio-categorias.html',
  styleUrls: ['./funcionario-relatorio-categorias.css']
})
export class FuncionarioRelatorioCategorias implements OnInit {
  receitas: ReceitaCategoriaDTO[] = [];
  totalReceita: number = 0;

  constructor(private relatorioService: RelatorioService) {}

  ngOnInit(): void {
    this.buscarReceitas();
  }

  buscarReceitas(): void {
    this.relatorioService.buscarReceitasPorCategoria().subscribe(data => {
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
