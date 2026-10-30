import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SolicitacaoService } from '../../../services/solicitacao.service';
import { EstadoSolicitacao } from '../../../shared';
import { FuncionarioSolicitacoes } from './funcionario-solicitacoes';

describe('FuncionarioSolicitacoes', () => {
  let component: FuncionarioSolicitacoes;
  let fixture: ComponentFixture<FuncionarioSolicitacoes>;
  let finalizarServico: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    finalizarServico = vi.fn().mockReturnValue(of({ id: 13, estado: EstadoSolicitacao.FINALIZADA }));

    await TestBed.configureTestingModule({
      imports: [FuncionarioSolicitacoes],
      providers: [
        provideRouter([]),
        {
          provide: SolicitacaoService,
          useValue: { finalizarServico }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FuncionarioSolicitacoes);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
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
});
