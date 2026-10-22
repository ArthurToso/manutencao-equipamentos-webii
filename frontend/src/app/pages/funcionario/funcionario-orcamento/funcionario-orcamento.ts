import { Component, inject, OnInit } from '@angular/core';
import { SolicitacaoDetailedFuncDTO as Solicitacao } from '../../../shared';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { NgxMaskDirective, provideNgxMask } from 'ngx-mask';
import { SolicitacaoService } from '../../../services/solicitacao.service';

@Component({
  imports: [CommonModule, RouterLink, ReactiveFormsModule, DatePipe, NgxMaskDirective],
  selector: 'app-funcionario-orcamento',
  styleUrl: './funcionario-orcamento.css',
  templateUrl: './funcionario-orcamento.html',
  providers: [provideNgxMask()]
})
export class FuncionarioOrcamento implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private solicitacaoService = inject(SolicitacaoService);

  solicitacaoId!: number;
  solicitacao?: Solicitacao;

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
    }
  }

  carregarSolicitacao(): void {
    this.solicitacaoService.buscarPorId(this.solicitacaoId).subscribe({
      next: (res) => {
        this.solicitacao = {
          id: res.id,
          dataHora: res.dataHora ? new Date(res.dataHora) : new Date(),
          descricaoEquipamento: res.descricaoEquipamento,
          categoriaEquipamento: { id: res.categoriaId, nome: res.categoriaNome },
          descricaoProblema: res.descricaoDefeito,
          estado: res.estado,
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
              estado: res.cliente.endereco?.uf,
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
              estado: '' as any,
              cep: ''
            }
          }
        };
      },
      error: (err) => {
        console.error(err);
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

    this.solicitacaoService.efetuarOrcamento(this.solicitacaoId, valorNumerico).subscribe({
      next: () => {
        const valorFormatado = valorNumerico.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
        this.mensagem = {
          titulo: 'Sucesso!',
          texto: `Orçamento enviado ao cliente.\nValor Orçado: ${valorFormatado}.`
        };
        this.processando = false;
      },
      error: (err) => {
        console.error(err);
        this.processando = false;
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
