import { Component, OnInit } from '@angular/core';
import { SolicitacaoDetailedDTO as Solicitacao, EstadoSolicitacao } from '../../../shared/';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { CommonModule } from '@angular/common';
import { SolicitacaoService } from '../../../services/solicitacao.service';

@Component({
  imports: [DatePipe, RouterLink, CommonModule],
  selector: 'app-cliente-solicitacao',
  styleUrl: './cliente-solicitacao.css',
  templateUrl: './cliente-solicitacao.html',
})

export class ClienteSolicitacao implements OnInit{
  
  solicitacao?: Solicitacao;
  
  constructor(
    private route: ActivatedRoute,
    private solicitacaoService: SolicitacaoService
  ){}

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
        console.error('Erro ao buscar solicitação', err);
      }
    });
  }
}
