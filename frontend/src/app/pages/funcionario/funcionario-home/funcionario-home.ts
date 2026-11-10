import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SolicitacaoCardFuncDTO } from '../../../shared';
import { SolicitacaoService } from '../../../services/solicitacao.service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-funcionario-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './funcionario-home.html',
  styleUrl: './funcionario-home.css'
})
export class FuncionarioHome implements OnInit {
  private cdr = inject(ChangeDetectorRef);

  solicitacoes: SolicitacaoCardFuncDTO[] = [];
  carregando = false;
  erroCarregamento: string | null = null;

  constructor(private solicitacaoService: SolicitacaoService) {}

  ngOnInit(): void {
    this.carregarAbertas();
  }

  carregarAbertas(): void {
    this.carregando = true;
    this.erroCarregamento = null;
    this.solicitacaoService.buscarAbertas().pipe(
      finalize(() => {
        this.carregando = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (dados) => {
        this.solicitacoes = dados.map(item => ({
          ...item,
          dataHora: item.dataHora ? new Date(item.dataHora) : item.dataHora
        }));
      },
      error: (err) => {
        console.error(err);
        this.erroCarregamento = err.error?.mensagem
          ?? 'Não foi possível carregar as solicitações. Tente novamente.';
      }
    });
  }

  getEquipamentoLimitado(descricao: string): string {
    if (!descricao) return '';
    return descricao.length > 30 ? descricao.substring(0, 30) + '...' : descricao;
  }
}
