import { Component, OnInit } from '@angular/core';
import { EstadoSolicitacao, SolicitacaoDetailedDTO } from '../../../shared';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { SolicitacaoService } from '../../../services/solicitacao.service';

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
  erro: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private solicitacaoService: SolicitacaoService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.solicitacaoService.buscarPorId(id).subscribe({
      next: (res) => {
        this.solicitacao = {
          id: res.id,
          descricaoEquipamento: res.descricaoEquipamento,
          categoriaEquipamento: { id: res.categoriaId, nome: res.categoriaNome },
          descricaoProblema: res.descricaoDefeito,
          dataHora: new Date(res.dataHora),
          estado: res.estado as EstadoSolicitacao,
          valorOrcado: res.valorOrcamento,
          motivoRejeicao: res.motivoRejeicao,
          historico: res.historico ? res.historico.map((h: any, index: number) => ({
            id: index,
            estado: h.estado as EstadoSolicitacao,
            dataHora: new Date(h.dataHora),
            userResponsavel: h.nomeFuncionario ? { id: 0, nome: h.nomeFuncionario, email: '' } : { id: 0, nome: 'Cliente/Sistema', email: '' }
          })) : []
        };
      },
      error: (err) => {
        this.erro = err.error?.mensagem ?? 'Não foi possível carregar a solicitação.';
      }
    });
  }

  confirmarPagamento(solicitacao: SolicitacaoDetailedDTO): void {
    if (this.processando) return;
    this.processando = true;
    this.erro = null;

    this.solicitacaoService.pagarServico(solicitacao.id).subscribe({
      next: () => {
        const agora = new Date();
        this.solicitacao!.estado = this.estadoSolicitacao.PAGA;
        this.pagamentoConfirmado = true;
        this.dataPagamento = agora;
        this.processando = false;
      },
      error: (err) => {
        this.processando = false;
        this.erro = err.error?.mensagem ?? 'Não foi possível confirmar o pagamento.';
      }
    });
  }
}
