import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RelatorioService, ReceitaDiaDTO } from '../../../services/relatorio.service';

@Component({
  selector: 'app-funcionario-relatorio-receitas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './funcionario-relatorio-receitas.html',
  styleUrls: ['./funcionario-relatorio-receitas.css']
})
export class FuncionarioRelatorioReceitas implements OnInit {
  dataInicial: string = '';
  dataFinal: string = '';
  receitas: ReceitaDiaDTO[] = [];
  totalReceita: number = 0;

  constructor(private relatorioService: RelatorioService) {}

  ngOnInit(): void {
    this.buscarReceitas();
  }

  buscarReceitas(): void {
    this.relatorioService.buscarReceitasPorDia(this.dataInicial, this.dataFinal).subscribe(data => {
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
