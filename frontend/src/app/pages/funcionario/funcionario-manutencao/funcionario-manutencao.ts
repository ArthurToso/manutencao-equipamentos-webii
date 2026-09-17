import { Component, inject, OnInit } from '@angular/core';
import { EstadoUF, SolicitacaoDetailedFuncDTO as Solicitacao, EstadoSolicitacao } from '../../../shared';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';

const mock: Solicitacao[] = [
  {
    id: 8,
    dataHora: new Date('2026-08-18T09:55:00'),
    descricaoEquipamento: 'Monitor LG UltraWide 29 polegadas',
    categoriaEquipamento: {id: 3, nome: 'Monitor'},
    descricaoProblema: 'Tela piscando',
    estado: EstadoSolicitacao.APROVADA,
    historico: [],
    cliente: {
      id: 2,
      nome: 'Joana Silva',
      email: 'joana@gmail.com',
      telefone: '41999999999',
      cpf: '09876543211',
      endereco: {
        logradouro: 'Rua das Flores',
        numero: 123,
        complemento: '',
        bairro: 'Centro',
        cidade: 'Curitiba',
        estado: EstadoUF.PR,
        cep: '80000000'
      }
    }
  },
  {
    id: 10,
    dataHora: new Date('2026-08-16T08:40:00'),
    descricaoEquipamento: 'Mouse Razer',
    categoriaEquipamento: {id: 4, nome: 'Mouse'},
    descricaoProblema: 'Clique duplo falhando',
    estado: EstadoSolicitacao.REDIRECIONADA,
    historico: [],
    cliente: {
      id: 1,
      nome: 'José',
      email: 'jose@gmail.com',
      telefone: '41988888888',
      cpf: '12345678900',
      endereco: {
        logradouro: 'Av Silva',
        numero: 10,
        complemento: '',
        bairro: 'Centro',
        cidade: 'Curitiba',
        estado: EstadoUF.PR,
        cep: '80000000'
      }
    }
  }
];

const mockFuncionarios = [
  { id: 2, nome: 'Maria Silva' },
  { id: 3, nome: 'Carlos Souza' }
];

@Component({
  imports: [CommonModule, RouterLink, ReactiveFormsModule, DatePipe],
  selector: 'app-funcionario-manutencao',
  styleUrl: './funcionario-manutencao.css',
  templateUrl: './funcionario-manutencao.html'
})
export class FuncionarioManutencao implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  solicitacaoId!: number;
  solicitacao?: Solicitacao; 
  funcionarios = mockFuncionarios;

  processando = false;
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
    if(id) this.solicitacaoId = id;
    this.carregarSolicitacao();
  }

  carregarSolicitacao(): void {
    this.solicitacao = mock.find((item) => item.id === this.solicitacaoId);
  }

  salvarManutencao(): void {
    if(this.formManutencao.invalid) return;

    this.processando = true;
    
    setTimeout(() => {
      this.mensagem = {
        titulo : 'Manutenção Efetuada!',
        texto : 'A manutenção foi registrada com sucesso e o estado alterado para ARRUMADA.'
      };
      this.processando = false;
    }, 500);
  }
  
  abrirModalRedirecionar(): void {
    this.exibirModalRedirecionar = true;
  }

  fecharModalRedirecionar(): void {
    this.exibirModalRedirecionar = false;
    this.formRedirecionar.reset();
  }

  salvarRedirecionamento(): void {
    if (this.formRedirecionar.invalid) return;

    this.processando = true;
    const funcSelecionado = this.funcionarios.find(f => f.id == this.formRedirecionar.value.funcionarioId);

    setTimeout(() => {
      this.mensagem = {
        titulo : 'Solicitação Redirecionada',
        texto : `A solicitação foi redirecionada para ${funcSelecionado?.nome}.`
      };
      this.processando = false;
      this.exibirModalRedirecionar = false;
    }, 500);
  }

  fecharMensagem(): void {
    this.mensagem = null;
    this.router.navigate(['/funcionario/solicitacoes']);
  }
}
