import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { EstadoSolicitacao, HistoricoApiDTO, SolicitacaoDetailedDTO, mapearEstadoSolicitacao } from '../../../shared';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { SolicitacaoService } from '../../../services/solicitacao.service';
import { finalize } from 'rxjs';

@Component({
  imports: [DatePipe, RouterLink, CommonModule],
  selector: 'app-cliente-pagar-servico',
  styleUrl: './cliente-pagar-servico.css',
  templateUrl: './cliente-pagar-servico.html',
})
export class ClientePagarServico implements OnInit {

  solicitacao?: SolicitacaoDetailedDTO;
  estadoSolicitacao = EstadoSolicitacao;
  pagamentoConfirmado: boolean = false;
  dataPagamento?: Date;
  processando: boolean = false;
  carregando = true;
  erroCarregamento: string | null = null;
  erro: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private solicitacaoService: SolicitacaoService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam || !/^\d+$/.test(idParam) || Number(idParam) <= 0) {
      this.carregando = false;
      this.erroCarregamento = 'Identificador da solicitação inválido.';
      this.cdr.markForCheck();
      return;
    }

    this.solicitacaoService.buscarPorId(Number(idParam)).pipe(
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
          descricaoManutencao: res.descricaoManutencao ?? undefined,
          orientacoesCliente: res.orientacoesCliente ?? undefined,
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
        console.error('Erro ao carregar solicitação para pagamento', err);
        if (err.status === 404 || err.status === 403) {
          this.erroCarregamento = 'Solicitação não encontrada.';
        } else {
          this.erroCarregamento = err.error?.mensagem
            ?? 'Não foi possível carregar os dados da solicitação. Tente novamente.';
        }
      }
    });
  }

  confirmarPagamento(solicitacao: SolicitacaoDetailedDTO): void {
    if (this.processando) return;
    this.processando = true;
    this.erro = null;

    this.solicitacaoService.pagarServico(solicitacao.id).pipe(
      finalize(() => {
        this.processando = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        const agora = new Date();
        this.solicitacao!.estado = this.estadoSolicitacao.PAGA;
        this.pagamentoConfirmado = true;
        this.dataPagamento = agora;
      },
      error: (err) => {
        this.erro = err.error?.mensagem ?? 'Não foi possível confirmar o pagamento.';
      }
    });
  }
}
