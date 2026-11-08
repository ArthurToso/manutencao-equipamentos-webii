import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SolicitacaoService } from '../../../services/solicitacao.service';
import { EstadoSolicitacao, SolicitacaoCardFuncDTO } from '../../../shared';
import { FuncionarioSolicitacoes } from './funcionario-solicitacoes';

describe('FuncionarioSolicitacoes', () => {
  let component: FuncionarioSolicitacoes;
  let fixture: ComponentFixture<FuncionarioSolicitacoes>;
  let finalizarServico: ReturnType<typeof vi.fn>;
  let listarTodas: ReturnType<typeof vi.fn>;

  const mockSolicitacoes: SolicitacaoCardFuncDTO[] = [
    {
      id: 6,
      dataHora: new Date('2026-09-16T08:40:00'),
      nomeCliente: 'José',
      descricaoEquipamento: 'Impressora Epson L3250',
      estado: EstadoSolicitacao.ABERTA
    },
    {
      id: 10,
      dataHora: new Date('2026-08-16T08:40:00'),
      nomeCliente: 'José',
      descricaoEquipamento: 'Mouse Razer',
      estado: EstadoSolicitacao.REDIRECIONADA
    },
    {
      id: 13,
      dataHora: new Date('2026-08-15T09:55:00'),
      nomeCliente: 'Maria',
      descricaoEquipamento: 'Teclado Redragon',
      estado: EstadoSolicitacao.PAGA
    }
  ];

  beforeEach(async () => {
    finalizarServico = vi.fn().mockReturnValue(of({ id: 13, estado: EstadoSolicitacao.FINALIZADA }));
    listarTodas = vi.fn().mockImplementation(() => of(mockSolicitacoes.map(s => ({ ...s }))));

    await TestBed.configureTestingModule({
      imports: [FuncionarioSolicitacoes],
      providers: [
        provideRouter([]),
        {
          provide: SolicitacaoService,
          useValue: { finalizarServico, listarTodas }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FuncionarioSolicitacoes);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create and load all solicitations on init', () => {
    expect(component).toBeTruthy();
    expect(listarTodas).toHaveBeenCalledWith('TODAS', undefined, undefined);
    expect(component.solicitacoes.length).toBe(3);
    expect(component.carregando).toBe(false);
  });

  it('applies filter HOJE when selecting option hoje and applying', () => {
    component.selecionarOpcao('hoje');
    expect(component.opcaoAtiva).toBe('hoje');

    component.aplicarFiltroData();

    expect(listarTodas).toHaveBeenCalledWith('HOJE', undefined, undefined);
    expect(component.dateFilferActive).toBe(true);
    expect(component.dateFilterOpen).toBe(false);
  });

  it('applies filter PERIODO when dates are selected', () => {
    component.selecionarOpcao('selecionar');
    component.dataInicio = new Date(2026, 8, 1);
    component.dataFim = new Date(2026, 8, 30);

    component.aplicarFiltroData();

    expect(listarTodas).toHaveBeenCalledWith('PERIODO', '2026-09-01', '2026-09-30');
    expect(component.dateFilferActive).toBe(true);
  });

  it('cleans filter and resets to TODAS', () => {
    component.dataInicio = new Date(2026, 8, 1);
    component.dataFim = new Date(2026, 8, 30);
    component.dateFilferActive = true;

    component.limparFiltro();

    expect(component.dataInicio).toBeNull();
    expect(component.dataFim).toBeNull();
    expect(component.dateFilferActive).toBe(false);
    expect(listarTodas).toHaveBeenCalledWith('TODAS', undefined, undefined);
  });

  it('finalizes a paid solicitation through the service and updates its state', () => {
    component.finalizarSolicitacao(13);

    expect(finalizarServico).toHaveBeenCalledWith(13);
    expect(component.solicitacoes.find(solicitacao => solicitacao.id === 13)?.estado)
      .toBe(EstadoSolicitacao.FINALIZADA);
    expect(component.finalizandoId).toBeNull();
  });

  it('keeps the solicitation paid and reports the error when finalization fails', () => {
    finalizarServico.mockReturnValue(throwError(() => ({
      error: { mensagem: 'A solicitação não pode ser finalizada.' }
    })));

    component.finalizarSolicitacao(13);

    expect(component.solicitacoes.find(solicitacao => solicitacao.id === 13)?.estado)
      .toBe(EstadoSolicitacao.PAGA);
    expect(component.erroFinalizacao).toBe('A solicitação não pode ser finalizada.');
    expect(component.finalizandoId).toBeNull();
  });

  it('does not request finalization for a solicitation that is not paid', () => {
    component.finalizarSolicitacao(6);

    expect(finalizarServico).not.toHaveBeenCalled();
  });

  it('displays loading error when listarTodas fails', () => {
    listarTodas.mockReturnValue(throwError(() => ({
      error: { mensagem: 'Erro ao carregar solicitações.' }
    })));

    component.carregarSolicitacoes('TODAS');

    expect(component.erroCarregamento).toBe('Erro ao carregar solicitações.');
    expect(component.carregando).toBe(false);
  });

  it('limits equipment description to 30 characters', () => {
    const curta = 'Mouse USB';
    const longa = 'Notebook Dell Inspiron 15 com teclado numérico retroiluminado';

    expect(component.getEquipamentoLimitado(curta)).toBe(curta);
    expect(component.getEquipamentoLimitado(longa)).toBe('Notebook Dell Inspiron 15 com ...');
  });
});
