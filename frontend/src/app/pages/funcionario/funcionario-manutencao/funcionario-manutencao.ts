import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import {
  EstadoSolicitacao,
  EstadoUF,
  FuncionarioDTO,
  mapearEstadoSolicitacao,
  SolicitacaoDetailedFuncDTO as Solicitacao
} from '../../../shared';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { SolicitacaoService } from '../../../services/solicitacao.service';
import { FuncionarioService } from '../../../services/funcionario-service';
import { AuthService } from '../../../services/auth.service';
import { finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  imports: [CommonModule, RouterLink, ReactiveFormsModule, DatePipe],
  selector: 'app-funcionario-manutencao',
  styleUrl: './funcionario-manutencao.css',
  templateUrl: './funcionario-manutencao.html'
})
export class FuncionarioManutencao implements OnInit {
  private cdr = inject(ChangeDetectorRef);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private solicitacaoService = inject(SolicitacaoService);
  private funcionarioService = inject(FuncionarioService);
  private authService = inject(AuthService);

  solicitacaoId!: number;
  solicitacao?: Solicitacao;
  funcionarios: FuncionarioDTO[] = [];
  erroCarregamento: string | null = null;

  processando = false;
  erroOperacao: string | null = null;
  mensagem: { titulo: string; texto: string } | null = null;
  exibirModalRedirecionar = false;

  formManutencao: FormGroup = this.fb.group({
    descricaoManutencao: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(1000)]],
    orientacoesCliente: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(1000)]]
  });

  formRedirecionar: FormGroup = this.fb.group({
    funcionarioId: ['', Validators.required]
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.solicitacaoId = id;
      this.carregarSolicitacao();
    } else {
      this.erroCarregamento = 'Identificador da solicitação inválido.';
    }
    this.carregarFuncionarios();
  }

  carregarSolicitacao(): void {
    this.solicitacaoService.buscarPorId(this.solicitacaoId).pipe(
      finalize(() => {
        this.cdr.detectChanges();
      })
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
          descricaoManutencao: res.descricaoManutencao ?? undefined,
          orientacoesCliente: res.orientacoesCliente ?? undefined,
          estado: mapearEstadoSolicitacao(res.estado),
          historico: (res.historico || []).flatMap((h, index) => {
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

  get podeEfetuarManutencao(): boolean {
    return this.solicitacao?.estado === EstadoSolicitacao.APROVADA
      || this.solicitacao?.estado === EstadoSolicitacao.REDIRECIONADA;
  }

  carregarFuncionarios(): void {
    const usuarioLogado = this.authService.usuarioAtual();
    this.funcionarioService.listar().subscribe({
      next: (lista) => {
        this.funcionarios = lista.filter((func) => {
          if (!usuarioLogado) {
            return true;
          }
          if (usuarioLogado.funcionarioId != null && func.id === usuarioLogado.funcionarioId) {
            return false;
          }
          if (usuarioLogado.email && func.email && func.email.toLowerCase() === usuarioLogado.email.toLowerCase()) {
            return false;
          }
          return true;
        });
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  salvarManutencao(): void {
    if (!this.solicitacao || !this.podeEfetuarManutencao || this.processando) return;
    if (this.formManutencao.invalid) {
      this.formManutencao.markAllAsTouched();
      return;
    }

    const dados = {
      descricaoManutencao: String(this.formManutencao.value.descricaoManutencao).trim(),
      orientacoesCliente: String(this.formManutencao.value.orientacoesCliente).trim()
    };
    this.processando = true;
    this.erroOperacao = null;

    this.solicitacaoService.efetuarManutencao(this.solicitacaoId, dados).pipe(
      finalize(() => {
        this.processando = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        if (this.solicitacao) {
          this.solicitacao.estado = EstadoSolicitacao.ARRUMADA;
          this.solicitacao.descricaoManutencao = dados.descricaoManutencao;
          this.solicitacao.orientacoesCliente = dados.orientacoesCliente;
        }
        this.mensagem = {
          titulo: 'Manutenção Efetuada!',
          texto: 'A manutenção foi registrada e a solicitação passou para ARRUMADA.'
        };
      },
      error: (err: HttpErrorResponse) => {
        this.erroOperacao = this.mensagemErro(err, 'Não foi possível registrar a manutenção. Tente novamente.');
      }
    });
  }

  abrirModalRedirecionar(): void {
    this.exibirModalRedirecionar = true;
  }

  fecharModalRedirecionar(): void {
    this.exibirModalRedirecionar = false;
    this.formRedirecionar.reset({ funcionarioId: '' });
  }

  salvarRedirecionamento(): void {
    if (this.formRedirecionar.invalid || this.processando) return;

    const funcionarioDestinoId = Number(this.formRedirecionar.value.funcionarioId);
    if (!funcionarioDestinoId) return;

    this.processando = true;
    this.erroOperacao = null;
    const funcSelecionado = this.funcionarios.find((f) => f.id === funcionarioDestinoId);

    this.solicitacaoService.redirecionar(this.solicitacaoId, { funcionarioId: funcionarioDestinoId }).pipe(
      finalize(() => {
        this.processando = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        this.exibirModalRedirecionar = false;
        this.formRedirecionar.reset({ funcionarioId: '' });
        if (this.solicitacao) {
          this.solicitacao.estado = EstadoSolicitacao.REDIRECIONADA;
        }
        this.mensagem = {
          titulo: 'Solicitação Redirecionada',
          texto: `A solicitação foi redirecionada para ${funcSelecionado?.nome || 'o funcionário selecionado'}.`
        };
      },
      error: (err: HttpErrorResponse) => {
        this.erroOperacao = this.mensagemErro(err, 'Não foi possível redirecionar a solicitação. Tente novamente.');
      }
    });
  }

  fecharMensagem(): void {
    this.mensagem = null;
    this.router.navigate(['/funcionario/solicitacoes']);
  }

  private mensagemErro(err: HttpErrorResponse, mensagemPadrao: string): string {
    if (typeof err.error?.mensagem === 'string' && err.error.mensagem.trim()) {
      return err.error.mensagem;
    }
    return mensagemPadrao;
  }
}
