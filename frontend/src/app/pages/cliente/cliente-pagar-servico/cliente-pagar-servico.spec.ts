import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { SolicitacaoService } from '../../../services/solicitacao.service';
import { EstadoSolicitacao } from '../../../shared';
import { ClientePagarServico } from './cliente-pagar-servico';

describe('ClientePagarServico', () => {
  let component: ClientePagarServico;
  let fixture: ComponentFixture<ClientePagarServico>;
  let buscarPorId: ReturnType<typeof vi.fn>;
  let solicitacaoService: {
    buscarPorId: ReturnType<typeof vi.fn>;
    pagarServico: ReturnType<typeof vi.fn>;
  };

  const respostaSolicitacao = {
    id: 42,
    descricaoEquipamento: 'Notebook',
    categoriaId: 1,
    categoriaNome: 'Informática',
    descricaoDefeito: 'Não liga',
    dataHora: '2026-09-15T10:00:00',
    estado: 'ARRUMADA',
    clienteId: 3,
    valorOrcamento: 250,
    descricaoManutencao: 'Troca da fonte',
    orientacoesCliente: 'Utilizar filtro de linha',
    historico: []
  };

  async function criarComRota(id: string): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [ClientePagarServico],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id }) } }
        },
        { provide: SolicitacaoService, useValue: solicitacaoService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClientePagarServico);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(() => {
    buscarPorId = vi.fn().mockReturnValue(of(respostaSolicitacao));
    solicitacaoService = {
      buscarPorId,
      pagarServico: vi.fn().mockReturnValue(of({ id: 42, estado: 'PAGA' }))
    };
  });

  it('shows loading until the request observable emits, then maps the response', async () => {
    const resposta = new Subject<typeof respostaSolicitacao>();
    buscarPorId.mockReturnValue(resposta);
    await criarComRota('42');

    expect(component.carregando).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Carregando os dados da solicitação');

    resposta.next(respostaSolicitacao);
    resposta.complete();
    fixture.detectChanges();

    expect(component.carregando).toBe(false);
    expect(component.solicitacao?.id).toBe(42);
    expect(component.solicitacao?.estado).toBe(EstadoSolicitacao.ARRUMADA);
    expect(component.solicitacao?.descricaoManutencao).toBe('Troca da fonte');
    expect(component.solicitacao?.orientacoesCliente).toBe('Utilizar filtro de linha');
  });

  it('shows the API error instead of presenting a missing-request message while loading', async () => {
    buscarPorId.mockReturnValue(throwError(() => ({
      status: 500,
      error: { mensagem: 'Falha ao consultar a solicitação' }
    })));
    await criarComRota('42');

    expect(component.carregando).toBe(false);
    expect(component.erroCarregamento).toBe('Falha ao consultar a solicitação');
    expect(fixture.nativeElement.textContent).toContain('Falha ao consultar a solicitação');
  });

  it('does not call the API for an invalid solicitation id', async () => {
    await criarComRota('abc');

    expect(buscarPorId).not.toHaveBeenCalled();
    expect(component.carregando).toBe(false);
    expect(component.erroCarregamento).toBe('Identificador da solicitação inválido.');
    expect(fixture.nativeElement.textContent).toContain('Identificador da solicitação inválido.');
  });

  it('shows not found for a missing solicitation', async () => {
    buscarPorId.mockReturnValue(throwError(() => ({ status: 404, error: {} })));
    await criarComRota('42');

    expect(component.erroCarregamento).toBe('Solicitação não encontrada.');
  });

  it('shows not found when the customer cannot access the solicitation', async () => {
    buscarPorId.mockReturnValue(throwError(() => ({ status: 403, error: {} })));
    await criarComRota('42');

    expect(component.erroCarregamento).toBe('Solicitação não encontrada.');
  });

  it('updates the screen after payment succeeds', async () => {
    await criarComRota('42');

    component.confirmarPagamento(component.solicitacao!);
    fixture.detectChanges();

    expect(solicitacaoService.pagarServico).toHaveBeenCalledWith(42);
    expect(component.processando).toBe(false);
    expect(component.pagamentoConfirmado).toBe(true);
    expect(component.solicitacao?.estado).toBe(EstadoSolicitacao.PAGA);
    expect(fixture.nativeElement.textContent).toContain('Pagamento Confirmado');
  });

  it('stops processing and shows the API error when payment fails', async () => {
    solicitacaoService.pagarServico.mockReturnValueOnce(throwError(() => ({
      error: { mensagem: 'A solicitação já foi paga.' }
    })));
    await criarComRota('42');

    component.confirmarPagamento(component.solicitacao!);
    fixture.detectChanges();

    expect(component.processando).toBe(false);
    expect(component.pagamentoConfirmado).toBe(false);
    expect(component.erro).toBe('A solicitação já foi paga.');
    expect(fixture.nativeElement.textContent).toContain('A solicitação já foi paga.');
  });
});
