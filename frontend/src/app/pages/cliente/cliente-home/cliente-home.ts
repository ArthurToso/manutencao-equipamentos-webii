import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SolicitacaoCardDTO as Solicitacao } from '../../../shared';
import { EstadoSolicitacao } from '../../../shared/enums';
import { SolicitacaoService } from '../../../services/solicitacao.service';


@Component({
  selector: 'app-cliente-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './cliente-home.html',
  styleUrl: './cliente-home.scss'
})
export class ClienteHome implements OnInit {
  private solicitacaoService = inject(SolicitacaoService)
  estado = EstadoSolicitacao;

  solicitacoes = signal<Solicitacao[]>([]);
  carregando = signal(true);
  erro = signal<string | null>(null);

  ngOnInit(): void {
    this.carregarSolicitacoes();
  }

  carregarSolicitacoes(): void {
    this.solicitacaoService.listarMinhas().subscribe({
      next: (lista) => {
        this.solicitacoes.set(lista);
        this.carregando.set(false);
      },
      error: (err) => {
        this.erro.set(err.error?.mensagem ?? 'Não foi possível carregar suas solicitações.');
        this.carregando.set(false);
      }
    });
  }

  getEquipamentoLimitado(descricao: string): string {
    return descricao.length > 30 ? descricao.substring(0, 30) + '...' : descricao;
  }

  resgatarServico(solicitacao: Solicitacao): void {
    if (!confirm('Deseja realmente resgatar este serviço?')) {
      return;
    }
    this.solicitacaoService.resgatarServico(solicitacao.id).subscribe({
      next: () => {
        this.carregarSolicitacoes();
      },
      error: (err) => {
        this.erro.set(err.error?.mensagem ?? 'Não foi possível resgatar o serviço.');
      }
    });
  }

}
