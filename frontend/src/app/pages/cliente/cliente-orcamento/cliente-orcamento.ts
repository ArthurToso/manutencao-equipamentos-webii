import { ChangeDetectorRef, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SolicitacaoDetailedDTO, mapearEstadoSolicitacao } from '../../../shared';
import { EstadoSolicitacao } from '../../../shared/enums';
import { SolicitacaoService } from '../../../services/solicitacao.service';

type Etapa = 'orcamento' | 'rejeitar';
type EstadoTela = 'carregando' | 'pronto' | 'erro';

@Component({
  selector: 'app-cliente-orcamento',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './cliente-orcamento.html',
  styleUrl: './cliente-orcamento.css'
})
export class ClienteOrcamento implements OnInit {

  @ViewChild('btnOk') btnOk?: ElementRef<HTMLButtonElement>;

  solicitacao?: SolicitacaoDetailedDTO;
  readonly EstadoSolicitacao = EstadoSolicitacao;

  estadoTela: EstadoTela = 'carregando';
  mensagemErro: string | null = null;
  mostrarBotaoVoltar = false;

  etapa: Etapa = 'orcamento';
  motivoRejeicao = '';
  motivoTocado = false;

  processando = false;
  erroAprovacao: string | null = null;
  mensagem: { titulo: string; texto: string } | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private solicitacaoService: SolicitacaoService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam || !/^\d+$/.test(idParam) || Number(idParam) <= 0) {
      this.estadoTela = 'erro';
      this.mensagemErro = 'Solicitação não encontrada';
      this.mostrarBotaoVoltar = true;
      this.cdr.markForCheck();
      return;
    }

    this.carregarOrcamento(Number(idParam));
  }

  carregarOrcamento(id: number): void {
    this.estadoTela = 'carregando';
    this.mensagemErro = null;
    this.cdr.markForCheck();

    this.solicitacaoService.buscarOrcamento(id).subscribe({
      next: (res) => {
        this.solicitacao = {
          id: res.id,
          dataHora: new Date(res.dataHora),
          descricaoEquipamento: res.descricaoEquipamento,
          categoriaEquipamento: { id: res.categoriaId, nome: res.categoriaNome },
          descricaoProblema: res.descricaoDefeito,
          estado: mapearEstadoSolicitacao(res.estado),
          valorOrcado: res.valorOrcamento ?? 0,
          historico: []
        };
        this.estadoTela = 'pronto';
        this.erroAprovacao = null;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.estadoTela = 'erro';
        this.mostrarBotaoVoltar = true;
        const status = err.status;
        if (status === 404 || status === 403) {
          this.mensagemErro = 'Solicitação não encontrada';
        } else if (status === 409) {
          this.mensagemErro = err.error?.mensagem || 'A solicitação ainda não foi orçada';
        } else {
          this.mensagemErro = 'Não foi possível carregar o orçamento. Tente novamente.';
        }
        this.cdr.markForCheck();
      }
    });
  }

  formatarMoeda(valor: number): string {
    return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }).replace(/\u00a0/g, ' ');
  }

  irParaRejeicao(): void {
    this.etapa = 'rejeitar';
  }

  cancelarRejeicao(): void {
    this.etapa = 'orcamento';
    this.motivoRejeicao = '';
    this.motivoTocado = false;
  }

  aprovarServico(): void {
    const valor = this.solicitacao?.valorOrcado;
    if (!this.solicitacao || this.processando || !valor || valor <= 0) return;

    this.processando = true;
    this.erroAprovacao = null;
    this.cdr.markForCheck();

    this.solicitacaoService.aprovar(this.solicitacao.id).subscribe({
      next: (res) => {
        this.processando = false;
        const valorAprovado = res.valorOrcamento ?? this.solicitacao!.valorOrcado!;
        this.solicitacao!.estado = EstadoSolicitacao.APROVADA;
        this.mensagem = {
          titulo: 'Serviço Aprovado',
          texto: `Serviço Aprovado no Valor ${this.formatarMoeda(valorAprovado)}`
        };
        this.cdr.markForCheck();
        setTimeout(() => {
          this.btnOk?.nativeElement?.focus();
        }, 0);
      },
      error: (err) => {
        this.processando = false;
        this.erroAprovacao = err.error?.mensagem || 'Não foi possível aprovar a solicitação.';
        this.cdr.markForCheck();
        if (err.status === 409 && this.solicitacao) {
          this.carregarOrcamento(this.solicitacao.id);
        }
      }
    });
  }

  confirmarRejeicao(): void {
    // TODO RF007: ligar ao PUT /rejeitar
    this.motivoTocado = true;
    if (!this.motivoRejeicao.trim() || this.processando) return;

    this.processando = true;

    setTimeout(() => {
      this.processando = false;
      this.solicitacao!.estado = EstadoSolicitacao.REJEITADA;
      this.mensagem = {
        titulo: 'Serviço Rejeitado',
        texto: 'Serviço Rejeitado'
      };
      setTimeout(() => {
        this.btnOk?.nativeElement?.focus();
      }, 0);
    }, 500);
  }

  fecharMensagem(): void {
    this.mensagem = null;
    this.router.navigate(['/cliente/home']);
  }
}
