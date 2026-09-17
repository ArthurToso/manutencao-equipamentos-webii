import { Component, HostListener, OnInit } from '@angular/core';
import { SolicitacaoCardFuncDTO } from '../../../shared';
import { EstadoSolicitacao } from '../../../shared';
import { DatePipe, NgClass } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDatepickerModule, DateRange } from '@angular/material/datepicker';
import { provideNativeDateAdapter } from '@angular/material/core';

const mock: SolicitacaoCardFuncDTO[] = [
  { id: 6, dataHora: new Date('2026-09-16T08:40:00'), nomeCliente: 'José', descricaoEquipamento: 'Impressora Epson L3250', estado: EstadoSolicitacao.ABERTA },
  { id: 7, dataHora: new Date('2026-09-17T13:10:00'), nomeCliente: 'João', descricaoEquipamento: 'Notebook Lenovo IdeaPad 3', estado: EstadoSolicitacao.ORCADA },
  { id: 8, dataHora: new Date('2026-08-18T09:55:00'), nomeCliente: 'Joana', descricaoEquipamento: 'Monitor LG UltraWide 29 polegadas', estado: EstadoSolicitacao.APROVADA },
  { id: 9, dataHora: new Date('2026-09-15T11:20:00'), nomeCliente: 'Joaquina', descricaoEquipamento: 'Impressora HP', estado: EstadoSolicitacao.REJEITADA },
  { id: 10, dataHora: new Date('2026-08-16T08:40:00'), nomeCliente: 'José', descricaoEquipamento: 'Mouse Razer', estado: EstadoSolicitacao.REDIRECIONADA },
  { id: 11, dataHora: new Date('2026-08-17T13:10:00'), nomeCliente: 'João', descricaoEquipamento: 'Notebook Lenovo IdeaPad 3', estado: EstadoSolicitacao.ARRUMADA },
  { id: 12, dataHora: new Date('2026-08-18T09:55:00'), nomeCliente: 'Joana', descricaoEquipamento: 'PC Positivo i3 8GB', estado: EstadoSolicitacao.FINALIZADA },
  { id: 13, dataHora: new Date('2026-08-15T09:55:00'), nomeCliente: 'Maria', descricaoEquipamento: 'Teclado Redragon', estado: EstadoSolicitacao.PAGA }
]

@Component({
  imports: [DatePipe, RouterLink, NgClass, ReactiveFormsModule, MatFormFieldModule, MatDatepickerModule],
  providers: [
    provideNativeDateAdapter()
  ],
  selector: 'app-funcionario-solicitacoes',
  styleUrl: './funcionario-solicitacoes.css',
  templateUrl: './funcionario-solicitacoes.html',
})
export class FuncionarioSolicitacoes implements OnInit {
  estados = EstadoSolicitacao;

  todasSolicitacoes: SolicitacaoCardFuncDTO[] = [];

  solicitacoes: SolicitacaoCardFuncDTO[] = [];

  dateFilterOpen = false;
  dateFilferActive = false;

  opcaoAtiva: string = 'selecionar';
  dataInicio: Date | null = null;
  dataFim: Date | null = null;

  get periodoSelecionado(): DateRange<Date>{
    return new DateRange(this.dataInicio, this.dataFim);
  }

  ngOnInit(): void {
    this.carregarSolicitacoes();
  }

  carregarSolicitacoes(): void{
    //carregar do service
    const data = mock;

    if(data) 
      this.todasSolicitacoes = data.sort((a, b) => a.dataHora.getTime() - b.dataHora.getTime());

      this.solicitacoes = this.todasSolicitacoes;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.dateFilterOpen) return;

    const target = event.target as HTMLElement;
    
    const clickedInsideFiltro = target.closest('.filtro-container');
    const clickedInsideToggle = target.closest('.filter-group__header');

    if (!clickedInsideFiltro && !clickedInsideToggle) {
      this.dateFilterOpen = false;
    }
  }

  getEquipamentoLimitado(descricao: string): string {
    return descricao.length > 30 ? descricao.substring(0, 30) + '...' : descricao;
  }

  selecionarOpcao(opcao: string): void{
    const hoje = new Date();
    hoje.setHours(0,0,0,0);

    if(opcao === 'hoje'){
      this.dataInicio = new Date(hoje);
      this.dataFim = new Date(hoje);
      this.dataFim.setHours(23,59,59);
    }else if(opcao === 'semana'){
      const inicio = new Date(hoje);
      inicio.setDate(inicio.getDate() - 7);
      this.dataInicio = inicio;
      this.dataFim = new Date();
      this.dataFim.setHours(23,59,59);
    }else if(opcao === 'mes'){
      const inicio = new Date(hoje);
      inicio.setDate(inicio.getDate() - 30);
      this.dataInicio = inicio;
      this.dataFim = new Date();
      this.dataFim.setHours(23,59,59,9999)
    }else{
      this.dataInicio = null;
      this.dataFim = null;
    }
  }

  onDateSelected(date: Date | null): void{
    if (!date) return;
    
    this.opcaoAtiva = 'selecionar'; 

    if (!this.dataInicio || (this.dataInicio && this.dataFim)) {
      this.dataInicio = date;
      this.dataInicio.setHours(0, 0, 0, 0);
      this.dataFim = null;
    } else if (date >= this.dataInicio) {
      this.dataFim = date;
      this.dataFim.setHours(23, 59, 59, 999); 
    } else {
      this.dataInicio = date;
      this.dataInicio.setHours(0, 0, 0, 0);
    }
  }

  aplicarFiltroData(): void{
    if (!this.dataInicio) {
      this.solicitacoes = [...this.todasSolicitacoes];
      this.dateFilterOpen = false;
      this.dateFilferActive = false;
      return;
    }

    const fim = this.dataFim ? this.dataFim : new Date(this.dataInicio);
    if (!this.dataFim) {
        fim.setHours(23, 59, 59, 999);
    }

    this.solicitacoes = this.todasSolicitacoes.filter(s => {
      const dataReq = new Date(s.dataHora);
      return dataReq >= this.dataInicio! && dataReq <= fim;
    });

    this.dateFilterOpen = false; 
    this.dateFilferActive = true;
  }

  limparFiltro(): void{
    this.dataFim = null;
    this.dataInicio = null;
    this.aplicarFiltroData();
  }
}
