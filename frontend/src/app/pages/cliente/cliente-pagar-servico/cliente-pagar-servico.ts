import { Component, OnInit } from '@angular/core';
import { Categoria, EstadoSolicitacao, SolicitacaoDetailedDTO } from '../../../shared';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';

const CAT_NOTEBOOK: Categoria = { id: 1, nome: 'Notebook' }
const CAT_IMPRESSORA: Categoria = { id: 2, nome: 'Impressora' }

@Component({
  imports: [DatePipe, RouterLink, CommonModule],
  selector: 'app-cliente-pagar-servico',
  styleUrl: './cliente-pagar-servico.css',
  templateUrl: './cliente-pagar-servico.html',
})

export class ClientePagarServico implements OnInit {
  
  private readonly mock : SolicitacaoDetailedDTO [] = [
    {
        id: 1,
        descricaoEquipamento: 'Notebook Dell Inspiron 15',
        categoriaEquipamento: CAT_NOTEBOOK,
        descricaoProblema: 'Não liga, luz de energia não acende',
        dataHora: new Date('2026-09-01T10:30:00'),
        estado: EstadoSolicitacao.ARRUMADA,
        valorOrcado: 450,
        descricaoManutencao: 'Substituição da placa de energia',
        orientacoesCliente: 'Evitar usar o notebook sem estabilizador',
        historico: [
          { id: 1, estado: EstadoSolicitacao.ABERTA,   dataHora: new Date('2026-09-01T10:30:00'), userResponsavel: {id: 1, nome: 'Jose Santos', email: 'jose@gmail.com'} },
          { id: 2, estado: EstadoSolicitacao.ORCADA,   dataHora: new Date('2026-09-02T09:15:00'), userResponsavel: {id: 2, nome: 'Joao Costa', email: 'joao@gmail.com'} },
          { id: 3, estado: EstadoSolicitacao.APROVADA, dataHora: new Date('2026-09-02T14:00:00'), userResponsavel: {id: 3, nome: 'Lucas Moura', email: 'lucas@gmail.com'} },
          { id: 4, estado: EstadoSolicitacao.ARRUMADA, dataHora: new Date('2026-09-04T16:20:00'), userResponsavel: {id: 4, nome: 'Julia Motta', email: 'Julia@gmail.com'} }
        ]
      },
      {
        id: 2,
        descricaoEquipamento: 'Impressora HP',
        categoriaEquipamento: CAT_IMPRESSORA,
        descricaoProblema: 'Não está imprimindo',
        dataHora: new Date('2026-09-01T12:30:00'),
        estado: EstadoSolicitacao.ABERTA,
        historico: [
          { id: 1, estado: EstadoSolicitacao.ABERTA,   dataHora: new Date('2026-09-01T12:30:00'), userResponsavel: {id: 4, nome: 'Julia Motta', email: 'Julia@gmail.com'} }
        ]
      },
      {
        id: 3,
        descricaoEquipamento: 'Notebook Lenovo Thinkpad',
        categoriaEquipamento: CAT_NOTEBOOK,
        descricaoProblema: 'Não liga',
        dataHora: new Date('2026-09-01T13:30:00'),
        estado: EstadoSolicitacao.ORCADA,
        valorOrcado: 250,
        historico: [
          { id: 1, estado: EstadoSolicitacao.ABERTA,   dataHora: new Date('2026-09-01T13:30:00'), userResponsavel: {id: 2, nome: 'Joao Costa', email: 'joao@gmail.com'} },
          { id: 2, estado: EstadoSolicitacao.ORCADA,   dataHora: new Date('2026-09-02T15:15:00'), userResponsavel: {id: 5, nome: 'Maria Silva', email: 'maria@gmail.com'} }
        ]
      },
    ]

  constructor(private route: ActivatedRoute){}

  solicitacao?: SolicitacaoDetailedDTO
  estadoSolicitacao = EstadoSolicitacao
  pagamentoConfirmado: boolean = false
  dataPagamento?: Date
  
  confirmarPagamento(solicitacao: SolicitacaoDetailedDTO){
    const agora = new Date()

    solicitacao.estado = this.estadoSolicitacao.PAGA
    
    solicitacao.historico.push({
      id: solicitacao.historico.length + 1,
      estado: this.estadoSolicitacao.PAGA,
      dataHora: agora,
      userResponsavel: {id: 1, nome: "Arthur", email:"teste1234@gmail.com", perfil:'CLIENTE'}
    })

    this.pagamentoConfirmado = true
    this.dataPagamento = agora
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.solicitacao = this.mock.find(s => s.id === id);
  }

}
