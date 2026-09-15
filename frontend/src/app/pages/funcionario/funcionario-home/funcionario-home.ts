import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EstadoSolicitacao, SolicitacaoCardFuncDTO } from '../../../shared';


@Component({
  selector: 'app-funcionario-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './funcionario-home.html',
  styleUrl: './funcionario-home.css'
})
export class FuncionarioHome implements OnInit {
  solicitacoes: SolicitacaoCardFuncDTO[] = [];

  ngOnInit() {
    // RF011: somente solicitações no estado ABERTA aparecem aqui
    this.solicitacoes = [
      { id: 5, dataHora: new Date('2024-03-15T11:20:00'), nomeCliente: 'Joaquina', descricaoEquipamento: 'Teclado Mecânico Redragon', estado: EstadoSolicitacao.ABERTA },
      { id: 6, dataHora: new Date('2024-03-16T08:40:00'), nomeCliente: 'José', descricaoEquipamento: 'Impressora Epson L3250', estado: EstadoSolicitacao.ABERTA },
      { id: 7, dataHora: new Date('2024-03-17T13:10:00'), nomeCliente: 'João', descricaoEquipamento: 'Notebook Lenovo IdeaPad 3', estado: EstadoSolicitacao.ABERTA },
      { id: 8, dataHora: new Date('2024-03-18T09:55:00'), nomeCliente: 'Joana', descricaoEquipamento: 'Monitor LG UltraWide 29 polegadas', estado: EstadoSolicitacao.ABERTA }
    ];

    this.solicitacoes.sort((a, b) => a.dataHora.getTime() - b.dataHora.getTime());
  }

  getEquipamentoLimitado(descricao: string): string {
    return descricao.length > 30 ? descricao.substring(0, 30) + '...' : descricao;
  }
}
