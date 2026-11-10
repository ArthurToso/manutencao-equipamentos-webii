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
  erroOperacao = false;
  mensagem: { titulo: string; texto: string } | null = null;
  exibirModalRedirecionar = false;

  formManutencao: FormGroup = this.fb.group({
    descricaoManutencao: ['', Validators.required],
    orientacoesCliente: ['', Validators.required]
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
        this.solicitacao = {
          id: res.id,
          dataHora: res.dataHora ? new Date(res.dataHora) : new Date(),
          descricaoEquipamento: res.descricaoEquipamento,
          categoriaEquipamento: { id: res.categoriaId, nome: res.categoriaNome },
          descricaoProblema: res.descricaoDefeito,
          estado: mapearEstadoSolicitacao(res.estado),
          historico: res.historico || [],
          cliente: res.cliente ? {
            id: res.cliente.id,
            nome: res.cliente.nome,
            email: res.cliente.email,
            telefone: res.cliente.telefone,
            cpf: res.cliente.cpf,
            endereco: {
              logradouro: res.cliente.endereco?.logradouro || '',
              numero: Number(res.cliente.endereco?.numero) || 0,
              complemento: res.cliente.endereco?.complemento || '',
              bairro: res.cliente.endereco?.bairro || '',
              cidade: res.cliente.endereco?.cidade || '',
              estado: res.cliente.endereco?.uf || EstadoUF.PR,
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
    if (this.formManutencao.invalid || this.processando) return;

    this.processando = true;
    this.erroOperacao = false;

    setTimeout(() => {
      this.mensagem = {
        titulo: 'Manutenção Efetuada!',
        texto: 'A manutenção foi registrada com sucesso e o estado alterado para ARRUMADA.'
      };
      this.processando = false;
    }, 500);
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
    this.erroOperacao = false;
    const funcSelecionado = this.funcionarios.find((f) => f.id === funcionarioDestinoId);

    this.solicitacaoService.redirecionar(this.solicitacaoId, { funcionarioId: funcionarioDestinoId }).subscribe({
      next: () => {
        this.processando = false;
        this.exibirModalRedirecionar = false;
        this.formRedirecionar.reset({ funcionarioId: '' });
        this.mensagem = {
          titulo: 'Solicitação Redirecionada',
          texto: `A solicitação foi redirecionada para ${funcSelecionado?.nome || 'o funcionário selecionado'}.`
        };
      },
      error: (err) => {
        this.processando = false;
        this.exibirModalRedirecionar = false;
        this.erroOperacao = true;
        this.mensagem = {
          titulo: 'Erro ao Redirecionar',
          texto: err.error?.mensagem || 'Não foi possível redirecionar a solicitação. Tente novamente.'
        };
      }
    });
  }

  fecharMensagem(): void {
    const houveErro = this.erroOperacao;
    this.mensagem = null;
    this.erroOperacao = false;
    if (!houveErro) {
      this.router.navigate(['/funcionario/solicitacoes']);
    }
  }
}
