import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SolicitacaoDetailedDTO } from '../../../shared';
import { EstadoSolicitacao as Estado, EstadoSolicitacao } from '../../../shared/enums';


type Etapa = 'orcamento' | 'rejeitar';

@Component({
  selector: 'app-cliente-orcamento',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './cliente-orcamento.html',
  styleUrl: './cliente-orcamento.css'
})
export class ClienteOrcamento implements OnInit {

  private readonly mock: SolicitacaoDetailedDTO[] = [
    {
      id: 1,
      dataHora: new Date('2024-03-01T10:30:00'),
      descricaoEquipamento: 'Notebook Dell Inspiron 15 3000',
      categoriaEquipamento: {id: 1, nome:'Notebook'},
      descricaoProblema: 'Não liga mais desde ontem à noite. A tela não acende e o LED de energia não pisca.',
      estado: Estado.ORCADA,
      valorOrcado : 380,
      historico : []
    }
  ];

  solicitacao?: SolicitacaoDetailedDTO;

  etapa: Etapa = 'orcamento';
  motivoRejeicao = '';
  motivoTocado = false;

  processando = false;
  mensagem: { titulo: string; texto: string } | null = null;

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.solicitacao = this.mock.find(a => a.id === id) ?? this.mock[0];
  }

  formatarMoeda(valor: number): string {
    return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
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
    const valor = this.solicitacao!.valorOrcado;
    if (!this.solicitacao || this.processando || !valor) return;

    this.processando = true;

    setTimeout(() => {
      this.processando = false;
      this.solicitacao!.estado = Estado.APROVADA;
      this.mensagem = {
        titulo: 'Serviço Aprovado',
        texto: `Serviço Aprovado no Valor ${this.formatarMoeda(valor)}`
      };
    }, 500);
  }

  confirmarRejeicao(): void {
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
    }, 500);
  }

  fecharMensagem(): void {
    this.mensagem = null;
    this.router.navigate(['/cliente/home']);
  }
}
