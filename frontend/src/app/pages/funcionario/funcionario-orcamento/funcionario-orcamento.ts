import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import {
  EstadoUF,
  HistoricoApiDTO,
  mapearEstadoSolicitacao,
  SolicitacaoDetailedFuncDTO as Solicitacao
} from '../../../shared';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { NgxMaskDirective, provideNgxMask } from 'ngx-mask';
import { SolicitacaoService } from '../../../services/solicitacao.service';
import { finalize } from 'rxjs';

@Component({
  imports: [CommonModule, RouterLink, ReactiveFormsModule, DatePipe, NgxMaskDirective],
  selector: 'app-funcionario-orcamento',
  styleUrl: './funcionario-orcamento.css',
  templateUrl: './funcionario-orcamento.html',
  providers: [provideNgxMask()]
})
export class FuncionarioOrcamento implements OnInit {
  private cdr = inject(ChangeDetectorRef);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private solicitacaoService = inject(SolicitacaoService);

  solicitacaoId!: number;
  solicitacao?: Solicitacao;
  erroCarregamento: string | null = null;

  processando = false;
  mensagem: { titulo: string; texto: string } | null = null;

  formOrcamento: FormGroup = this.fb.group({
    valor: ['', [Validators.required, Validators.min(0.01)]]
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.solicitacaoId = id;
      this.carregarSolicitacao();
    } else {
      this.erroCarregamento = 'Identificador da solicitação inválido.';
    }
  }

  carregarSolicitacao(): void {
    this.solicitacaoService.buscarPorId(this.solicitacaoId).pipe(
      finalize(() => this.cdr.detectChanges())
    ).subscribe({
      next: (res) => {
        const estadoUf = Object.entries(EstadoUF).find(
          ([sigla]) => sigla === res.cliente?.endereco?.uf
        )?.[1] ?? EstadoUF.PR;
        this.solicitacao = {
          id: res.id,
          dataHora: res.dataHora ? new Date(res.dataHora) : new Date(),
          descricaoEquipamento: res.descricaoEquipamento,
          categoriaEquipamento: { id: res.categoriaId, nome: res.categoriaNome },
          descricaoProblema: res.descricaoDefeito,
          estado: mapearEstadoSolicitacao(res.estado),
          descricaoManutencao: res.descricaoManutencao ?? undefined,
          orientacoesCliente: res.orientacoesCliente ?? undefined,
          historico: (res.historico || []).flatMap((h: HistoricoApiDTO, index: number) => {
            if (!h.estado) {
              return [];
            }
            return [{
              id: h.id ?? index,
              estado: mapearEstadoSolicitacao(h.estado),
              dataHora: new Date(h.dataHora ?? ''),
              userResponsavel: {
                id: h.responsavel?.id ?? 0,
                nome: h.responsavel?.nome || h.nomeFuncionario || 'Cliente/Sistema',
                email: ''
              }
            }];
          }),
          cliente: res.cliente ? {
            id: res.cliente.id ?? 0,
            nome: res.cliente.nome ?? '',
            email: res.cliente.email ?? '',
            telefone: res.cliente.telefone ?? '',
            cpf: res.cliente.cpf ?? '',
            endereco: {
              logradouro: res.cliente.endereco?.logradouro || '',
              numero: Number(res.cliente.endereco?.numero) || 0,
              complemento: res.cliente.endereco?.complemento || '',
              bairro: res.cliente.endereco?.bairro || '',
              cidade: res.cliente.endereco?.cidade || '',
              estado: estadoUf,
              cep: res.cliente.endereco?.cep || ''
            }
          } : {
            nome: res.nomeCliente || '',
            email: '',
            telefone: '',
            cpf: '',
            endereco: {
              logradouro: '',
              numero: 0,
              complemento: '',
              bairro: '',
              cidade: '',
              estado: EstadoUF.PR,
              cep: ''
            }
          }
        };
      },
      error: (err) => {
        console.error(err);
        this.erroCarregamento = err.error?.mensagem
          || 'Não foi possível carregar os dados da solicitação. Tente novamente.';
      }
    });
  }

  salvarOrcamento(): void {
    if (this.formOrcamento.invalid || this.processando) return;

    const valorBruto = this.formOrcamento.value.valor;
    const valorNumerico = typeof valorBruto === 'number'
      ? valorBruto
      : parseFloat(String(valorBruto).replace(/\./g, '').replace(',', '.'));

    if (isNaN(valorNumerico) || valorNumerico <= 0) return;

    this.processando = true;

    this.solicitacaoService.efetuarOrcamento(this.solicitacaoId, valorNumerico).pipe(
      finalize(() => {
        this.processando = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        const valorFormatado = valorNumerico.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
        this.mensagem = {
          titulo: 'Sucesso!',
          texto: `Orçamento enviado ao cliente.\nValor Orçado: ${valorFormatado}.`
        };
      },
      error: (err) => {
        console.error(err);
        this.mensagem = {
          titulo: 'Erro',
          texto: 'Não foi possível salvar o orçamento. Verifique os dados e tente novamente.'
        };
      }
    });
  }

  fecharMensagem(): void {
    this.mensagem = null;
    this.router.navigate(['/funcionario/home']);
  }
}
