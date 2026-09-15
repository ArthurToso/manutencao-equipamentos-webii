import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SolicitacaoCardDTO as Solicitacao } from '../../../shared';
import { EstadoSolicitacao } from '../../../shared/enums';


@Component({
  selector: 'app-cliente-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './cliente-home.html',
  styleUrl: './cliente-home.scss'
})
export class ClienteHome implements OnInit {
  estado = EstadoSolicitacao;

  solicitacoes: Solicitacao[] = [];

  ngOnInit() {
    this.solicitacoes = [
      { id: 1, dataHora: new Date('2024-03-01T10:30:00'), descricaoEquipamento: 'Notebook Dell Inspiron', estado: this.estado.ORCADA },
      { id: 2, dataHora: new Date('2024-03-02T14:15:00'), descricaoEquipamento: 'Impressora HP LaserJet Pro M15w', estado: this.estado.APROVADA },
      { id: 3, dataHora: new Date('2024-03-05T09:00:00'), descricaoEquipamento: 'Desktop Gamer Custom', estado: this.estado.REJEITADA },
      { id: 4, dataHora: new Date('2024-03-10T16:45:00'), descricaoEquipamento: 'Monitor Samsung Odyssey G5 27 polegadas', estado: this.estado.ARRUMADA },
      { id: 5, dataHora: new Date('2024-03-15T11:20:00'), descricaoEquipamento: 'Teclado Mecânico', estado: this.estado.ABERTA }
    ];

    this.solicitacoes.sort((a, b) => a.dataHora.getTime() - b.dataHora.getTime());
  }

  getEquipamentoLimitado(descricao: string): string {
    return descricao.length > 30 ? descricao.substring(0, 30) + '...' : descricao;
  }
}
