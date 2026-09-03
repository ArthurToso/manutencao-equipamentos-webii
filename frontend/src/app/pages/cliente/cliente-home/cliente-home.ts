import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface Solicitacao {
  id: number;
  dataHora: Date;
  equipamento: string;
  estado: 'ABERTA' | 'ORÇADA' | 'APROVADA' | 'REJEITADA' | 'ARRUMADA' | 'PAGA' | 'FINALIZADA';
}

@Component({
  selector: 'app-cliente-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './cliente-home.html',
  styleUrl: './cliente-home.scss'
})
export class ClienteHome implements OnInit {
  solicitacoes: Solicitacao[] = [];

  ngOnInit() {
    this.solicitacoes = [
      { id: 1, dataHora: new Date('2024-03-01T10:30:00'), equipamento: 'Notebook Dell Inspiron', estado: 'ORÇADA' },
      { id: 2, dataHora: new Date('2024-03-02T14:15:00'), equipamento: 'Impressora HP LaserJet Pro M15w', estado: 'APROVADA' },
      { id: 3, dataHora: new Date('2024-03-05T09:00:00'), equipamento: 'Desktop Gamer Custom', estado: 'REJEITADA' },
      { id: 4, dataHora: new Date('2024-03-10T16:45:00'), equipamento: 'Monitor Samsung Odyssey G5 27 polegadas', estado: 'ARRUMADA' },
      { id: 5, dataHora: new Date('2024-03-15T11:20:00'), equipamento: 'Teclado Mecânico', estado: 'ABERTA' }
    ];

    this.solicitacoes.sort((a, b) => a.dataHora.getTime() - b.dataHora.getTime());
  }

  getEquipamentoLimitado(descricao: string): string {
    return descricao.length > 30 ? descricao.substring(0, 30) + '...' : descricao;
  }
}
