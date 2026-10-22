import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SolicitacaoCardFuncDTO } from '../../../shared';
import { SolicitacaoService } from '../../../services/solicitacao.service';

@Component({
  selector: 'app-funcionario-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './funcionario-home.html',
  styleUrl: './funcionario-home.css'
})
export class FuncionarioHome implements OnInit {
  solicitacoes: SolicitacaoCardFuncDTO[] = [];

  constructor(private solicitacaoService: SolicitacaoService) {}

  ngOnInit(): void {
    this.carregarAbertas();
  }

  carregarAbertas(): void {
    this.solicitacaoService.buscarAbertas().subscribe({
      next: (dados) => {
        this.solicitacoes = dados.map(item => ({
          ...item,
          dataHora: item.dataHora ? new Date(item.dataHora) : item.dataHora
        }));
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  getEquipamentoLimitado(descricao: string): string {
    if (!descricao) return '';
    return descricao.length > 30 ? descricao.substring(0, 30) + '...' : descricao;
  }
}
