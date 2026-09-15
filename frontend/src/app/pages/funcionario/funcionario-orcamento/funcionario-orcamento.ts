import { Component, inject } from '@angular/core';
import { EstadoUF, SolicitacaoDetailedFuncDTO as Solicitacao } from '../../../shared';
import { EstadoSolicitacao } from '../../../shared';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { NgxMaskDirective, provideNgxMask } from 'ngx-mask';


  const mock: Solicitacao[] = [
    {
      id: 6,
      dataHora: new Date('2026-09-12T12:30:00'),
      descricaoEquipamento: 'Impressora Epson L3250',
      categoriaEquipamento: {id: 2, nome: 'Impressora'},
      descricaoProblema: 'Não está imprimindo!',
      estado: EstadoSolicitacao.ABERTA,
      historico: [],
      cliente: {
        id: 1,
        nome: 'Jose Silva',
        email: 'jose@gmail.com',
        telefone: '41999241764',
        cpf: '09876524568',
        endereco: {
          logradouro: 'Av. Mariano Torres',
          numero: 324,
          complemento: 'casa verde',
          bairro: 'Centro',
          cidade: 'Curitiba',
          estado: EstadoUF.PR,
          cep: '80540200'
        }
      }
    }
  ];


@Component({
  imports: [CommonModule, RouterLink, ReactiveFormsModule, DatePipe, NgxMaskDirective],
  selector: 'app-funcionario-orcamento',
  styleUrl: './funcionario-orcamento.css',
  templateUrl: './funcionario-orcamento.html',
  providers: [provideNgxMask()]
})
export class FuncionarioOrcamento {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  solicitacaoId!: number;
  solicitacao?: Solicitacao; 

  processando = false;
  mensagem: { titulo: string; texto: string } | null = null;

  formOrcamento: FormGroup = this.fb.group({
    valor: ['', [Validators.required, Validators.min(0.01)]]
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if(id) this.solicitacaoId = id;
    this.carregarSolicitacao();
  }

  carregarSolicitacao(): void {
    this.solicitacao = mock.find((item) => item.id === this.solicitacaoId)
  }

  salvarOrcamento(): void{
    if(this.formOrcamento.invalid) return;

    const form = this.formOrcamento.value;
    const valor = form.valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    this.processando = true;

    

    this.mensagem = {
      titulo : 'Sucesso!',
      texto : `Orcamento enviado ao cliente.\n Valor Orçado: ${valor}.`
    };
    this.processando = false;
    

    console.log(valor);
  }
  
  fecharMensagem(): void {
    this.mensagem = null;
    this.router.navigate(['/funcionario/home']);
  }

}
