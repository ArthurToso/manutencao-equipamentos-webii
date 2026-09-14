import { Component, OnInit } from '@angular/core';
import { Solicitacao } from '../../../shared/models/solicitacao.model';
import { Usuario} from '../../../shared';
import { Categoria } from '../../../shared/models/categoria.model';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { CommonModule } from '@angular/common';

const CLIENTE_MOCK: Usuario = new Usuario(
  1, '12345678901', 'Maria Silva', 'maria@email.com', 1, '41999998888'
)
const CAT_NOTEBOOK: Categoria = { id: 1, nome: 'Notebook' }
const CAT_IMPRESSORA: Categoria = { id: 2, nome: 'Impressora' }

@Component({
  imports: [DatePipe, RouterLink, CommonModule],
  selector: 'app-cliente-solicitacao',
  styleUrl: './cliente-solicitacao.css',
  templateUrl: './cliente-solicitacao.html',
})

export class ClienteSolicitacao implements OnInit{
  
  private readonly mock: Solicitacao[] = [
    {
      id: 1,
      equipamento: 'Notebook Dell Inspiron 15',
      categoria: CAT_NOTEBOOK,
      defeito: 'Não liga, luz de energia não acende',
      dataHora: new Date('2026-09-01T10:30:00'),
      estado: 'ARRUMADA',
      valorOrcamento: 450,
      descricaoManutencao: 'Substituição da placa de energia',
      orientacoesCliente: 'Evitar usar o notebook sem estabilizador',
      cliente: CLIENTE_MOCK,
      historico: [
        { id: 1, estadoDestino: 'ABERTA',   dataHora: new Date('2026-09-01T10:30:00'), responsavel: 'Maria Silva' },
        { id: 2, estadoDestino: 'ORÇADA',   dataHora: new Date('2026-09-02T09:15:00'), responsavel: 'Carlos Souza' },
        { id: 3, estadoDestino: 'APROVADA', dataHora: new Date('2026-09-02T14:00:00'), responsavel: 'Maria Silva' },
        { id: 4, estadoDestino: 'ARRUMADA', dataHora: new Date('2026-09-04T16:20:00'), responsavel: 'Carlos Souza' }
      ]
    },
    {
      id: 2,
      equipamento: 'Impressora HP',
      categoria: CAT_IMPRESSORA,
      defeito: 'Não está imprimindo',
      dataHora: new Date('2026-09-01T12:30:00'),
      estado: 'ABERTA',
      cliente: CLIENTE_MOCK,
      historico: [
        { id: 1, estadoDestino: 'ABERTA',   dataHora: new Date('2026-09-01T12:30:00'), responsavel: 'Maria Silva' }
      ]
    },
    {
      id: 3,
      equipamento: 'Notebook Lenovo Thinkpad',
      categoria: CAT_NOTEBOOK,
      defeito: 'Não liga',
      dataHora: new Date('2026-09-01T13:30:00'),
      estado: 'ORÇADA',
      valorOrcamento: 250,
      cliente: CLIENTE_MOCK,
      historico: [
        { id: 1, estadoDestino: 'ABERTA',   dataHora: new Date('2026-09-01T13:30:00'), responsavel: 'Maria Silva' },
        { id: 2, estadoDestino: 'ORÇADA',   dataHora: new Date('2026-09-02T15:15:00'), responsavel: 'Carlos Souza' }
      ]
    },
  ]
  
  constructor(private route: ActivatedRoute){}

  solicitacao?: Solicitacao
  
  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.solicitacao = this.mock.find(s => s.id === id);
    //vou tratar undefined no template
  }

}
