import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { HistoricoApiDTO, SolicitacaoDetailedDTO as Solicitacao, mapearEstadoSolicitacao } from '../../../shared';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { CommonModule } from '@angular/common';
import { SolicitacaoService } from '../../../services/solicitacao.service';
import { finalize } from 'rxjs';

@Component({
  imports: [DatePipe, RouterLink, CommonModule],
  selector: 'app-cliente-solicitacao',
  styleUrl: './cliente-solicitacao.css',
  templateUrl: './cliente-solicitacao.html',
})
export class ClienteSolicitacao implements OnInit {
  private cdr = inject(ChangeDetectorRef);

  solicitacao?: Solicitacao;
  carregando = true;
  erroCarregamento: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private solicitacaoService: SolicitacaoService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.carregando = false;
      this.erroCarregamento = 'Identificador da solicitação inválido.';
      return;
    }

    this.solicitacaoService.buscarPorId(id).pipe(
      finalize(() => {
        this.carregando = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (res) => {
        this.solicitacao = {
          id: res.id,
          descricaoEquipamento: res.descricaoEquipamento,
          categoriaEquipamento: { id: res.categoriaId, nome: res.categoriaNome },
          descricaoProblema: res.descricaoDefeito,
          dataHora: new Date(res.dataHora),
          estado: mapearEstadoSolicitacao(res.estado),
          valorOrcado: res.valorOrcamento ?? undefined,
          motivoRejeicao: res.motivoRejeicao ?? undefined,
          historico: res.historico ? res.historico.map((h: HistoricoApiDTO, index: number) => ({
            id: index,
            estado: mapearEstadoSolicitacao(h.estado),
            dataHora: new Date(h.dataHora ?? ''),
            userResponsavel: h.nomeFuncionario ? { id: 0, nome: h.nomeFuncionario, email: '' } : { id: 0, nome: 'Cliente/Sistema', email: '' }
          })) : []
        };
      },
      error: (err) => {
        console.error('Erro ao buscar solicitação', err);
        this.erroCarregamento = err.error?.mensagem
          || 'Não foi possível carregar os dados da solicitação. Tente novamente.';
      }
    });
  }
}
