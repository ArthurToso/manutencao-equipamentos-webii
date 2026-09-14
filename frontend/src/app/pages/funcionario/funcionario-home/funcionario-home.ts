import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

interface SolicitacaoAberta {
  id: number;
  dataHora: Date;
  cliente: string;
  equipamento: string;
}

@Component({
  selector: 'app-funcionario-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './funcionario-home.html',
  styleUrl: './funcionario-home.css'
})
export class FuncionarioHome implements OnInit {
  solicitacoes: SolicitacaoAberta[] = [];

  ngOnInit() {
    // RF011: somente solicitações no estado ABERTA aparecem aqui
    this.solicitacoes = [
      { id: 5, dataHora: new Date('2024-03-15T11:20:00'), cliente: 'Joaquina', equipamento: 'Teclado Mecânico Redragon' },
      { id: 6, dataHora: new Date('2024-03-16T08:40:00'), cliente: 'José', equipamento: 'Impressora Epson L3250' },
      { id: 7, dataHora: new Date('2024-03-17T13:10:00'), cliente: 'João', equipamento: 'Notebook Lenovo IdeaPad 3' },
      { id: 8, dataHora: new Date('2024-03-18T09:55:00'), cliente: 'Joana', equipamento: 'Monitor LG UltraWide 29 polegadas' }
    ];

    this.solicitacoes.sort((a, b) => a.dataHora.getTime() - b.dataHora.getTime());
  }

  getEquipamentoLimitado(descricao: string): string {
    return descricao.length > 30 ? descricao.substring(0, 30) + '...' : descricao;
  }
}
