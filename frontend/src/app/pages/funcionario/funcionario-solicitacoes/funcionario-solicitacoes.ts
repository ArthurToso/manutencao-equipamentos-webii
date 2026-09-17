import { Component, OnInit } from '@angular/core';
import { SolicitacaoCardFuncDTO } from '../../../shared';
import { EstadoSolicitacao } from '../../../shared';
import { DatePipe, NgClass } from '@angular/common';
import { RouterLink } from '@angular/router';

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
  imports: [DatePipe, RouterLink, NgClass],
  selector: 'app-funcionario-solicitacoes',
  styleUrl: './funcionario-solicitacoes.css',
  templateUrl: './funcionario-solicitacoes.html',
})
export class FuncionarioSolicitacoes implements OnInit {
  estados = EstadoSolicitacao;

  solicitacoes: SolicitacaoCardFuncDTO[] = [];

  ngOnInit(): void {
    this.carregarSolicitacoes();
  }

  carregarSolicitacoes(): void{
    //carregar do service
    const data = mock;

    if(data) 
      this.solicitacoes = data.sort((a, b) => a.dataHora.getTime() - b.dataHora.getTime());
  }

  getEquipamentoLimitado(descricao: string): string {
    return descricao.length > 30 ? descricao.substring(0, 30) + '...' : descricao;
  }

  finalizarSolicitacao(id: number): void {
    const sol = this.solicitacoes.find(s => s.id === id);
    if(sol) {
      sol.estado = EstadoSolicitacao.FINALIZADA;
      window.alert(`Solicitação #${id} finalizada com sucesso!`);
    }
  }
}
