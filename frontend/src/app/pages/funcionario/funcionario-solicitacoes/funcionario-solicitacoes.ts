import { Component, HostListener, OnInit } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDatepickerModule, DateRange } from '@angular/material/datepicker';
import { provideNativeDateAdapter } from '@angular/material/core';
import { SolicitacaoCardFuncDTO, EstadoSolicitacao } from '../../../shared';
import { SolicitacaoService } from '../../../services/solicitacao.service';

function formatarDataIso(data: Date): string {
  const ano = data.getFullYear();
  const mes = String(data.getMonth() + 1).padStart(2, '0');
  const dia = String(data.getDate()).padStart(2, '0');
  return `${ano}-${mes}-${dia}`;
}

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
  constructor(private solicitacaoService: SolicitacaoService) {}

  estados = EstadoSolicitacao;

  solicitacoes: SolicitacaoCardFuncDTO[] = [];
  carregando = false;
  erroCarregamento: string | null = null;
  finalizandoId: number | null = null;
  erroFinalizacao: string | null = null;

  dateFilterOpen = false;
  dateFilferActive = false;

  opcaoAtiva: string = 'selecionar';
  dataInicio: Date | null = null;
  dataFim: Date | null = null;

  get periodoSelecionado(): DateRange<Date> {
    return new DateRange(this.dataInicio, this.dataFim);
  }

  ngOnInit(): void {
    this.carregarSolicitacoes('TODAS');
  }

  carregarSolicitacoes(
    filtro: 'HOJE' | 'PERIODO' | 'TODAS' = 'TODAS',
    dataInicial?: string,
    dataFinal?: string
  ): void {
    this.carregando = true;
    this.erroCarregamento = null;
    this.solicitacaoService.listarTodas(filtro, dataInicial, dataFinal).subscribe({
      next: (dados) => {
        this.solicitacoes = dados;
        this.carregando = false;
      },
      error: (err) => {
        this.erroCarregamento = err.error?.mensagem
          ?? 'Não foi possível carregar as solicitações. Tente novamente.';
        this.carregando = false;
      }
    });
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
    if (!descricao) return '';
    return descricao.length > 30 ? descricao.substring(0, 30) + '...' : descricao;
  }

  selecionarOpcao(opcao: string): void {
    this.opcaoAtiva = opcao;
    const hoje = new Date();

    if (opcao === 'hoje') {
      this.dataInicio = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate());
      this.dataFim = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate());
    } else if (opcao === 'semana') {
      const inicio = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate() - 7);
      this.dataInicio = inicio;
      this.dataFim = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate());
    } else if (opcao === 'mes') {
      const inicio = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate() - 30);
      this.dataInicio = inicio;
      this.dataFim = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate());
    } else {
      this.dataInicio = null;
      this.dataFim = null;
    }
  }

  onDateSelected(date: Date | null): void {
    if (!date) return;

    this.opcaoAtiva = 'selecionar';

    if (!this.dataInicio || (this.dataInicio && this.dataFim)) {
      this.dataInicio = new Date(date.getFullYear(), date.getMonth(), date.getDate());
      this.dataFim = null;
    } else if (date >= this.dataInicio) {
      this.dataFim = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    } else {
      this.dataInicio = new Date(date.getFullYear(), date.getMonth(), date.getDate());
      this.dataFim = null;
    }
  }

  aplicarFiltroData(): void {
    if (this.opcaoAtiva === 'hoje') {
      this.carregarSolicitacoes('HOJE');
      this.dateFilterOpen = false;
      this.dateFilferActive = true;
      return;
    }

    if (!this.dataInicio && !this.dataFim) {
      this.carregarSolicitacoes('TODAS');
      this.dateFilterOpen = false;
      this.dateFilferActive = false;
      return;
    }

    const dataInicial = this.dataInicio ? formatarDataIso(this.dataInicio) : undefined;
    const dataFinal = this.dataFim ? formatarDataIso(this.dataFim) : undefined;

    this.carregarSolicitacoes('PERIODO', dataInicial, dataFinal);
    this.dateFilterOpen = false;
    this.dateFilferActive = true;
  }

  limparFiltro(): void {
    this.opcaoAtiva = 'selecionar';
    this.dataInicio = null;
    this.dataFim = null;
    this.dateFilferActive = false;
    this.dateFilterOpen = false;
    this.carregarSolicitacoes('TODAS');
  }

  finalizarSolicitacao(id: number): void {
    const sol = this.solicitacoes.find(s => s.id === id);
    if (!sol || sol.estado !== EstadoSolicitacao.PAGA || this.finalizandoId !== null) {
      return;
    }

    this.finalizandoId = id;
    this.erroFinalizacao = null;
    this.solicitacaoService.finalizarServico(id).subscribe({
      next: () => {
        sol.estado = EstadoSolicitacao.FINALIZADA;
        this.finalizandoId = null;
      },
      error: (err) => {
        this.erroFinalizacao = err.error?.mensagem
          ?? 'Não foi possível finalizar a solicitação. Tente novamente.';
        this.finalizandoId = null;
      }
    });
  }
}
