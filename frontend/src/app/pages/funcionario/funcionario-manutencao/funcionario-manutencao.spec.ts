import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../services/auth.service';
import { FuncionarioService } from '../../../services/funcionario-service';
import { SolicitacaoService } from '../../../services/solicitacao.service';
import { EstadoSolicitacao } from '../../../shared';
import { FuncionarioManutencao } from './funcionario-manutencao';

describe('FuncionarioManutencao', () => {
  let component: FuncionarioManutencao;
  let fixture: ComponentFixture<FuncionarioManutencao>;
  let efetuarManutencao: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    efetuarManutencao = vi.fn().mockReturnValue(of({ id: 42, estado: 'ARRUMADA' }));

    await TestBed.configureTestingModule({
      imports: [FuncionarioManutencao],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '42' }) } }
        },
        {
          provide: SolicitacaoService,
          useValue: {
            buscarPorId: vi.fn().mockReturnValue(of({
              id: 42,
              descricaoEquipamento: 'Notebook',
              categoriaId: 1,
              categoriaNome: 'Informática',
              descricaoDefeito: 'Não liga',
              dataHora: '2026-09-15T10:00:00',
              estado: 'APROVADA',
              clienteId: 3,
              nomeCliente: 'Cliente',
              historico: []
            })),
            efetuarManutencao,
            redirecionar: vi.fn().mockReturnValue(of({ id: 42, estado: 'REDIRECIONADA' }))
          }
        },
        {
          provide: FuncionarioService,
          useValue: { listar: vi.fn().mockReturnValue(of([])) }
        },
        {
          provide: AuthService,
          useValue: { usuarioAtual: () => null }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FuncionarioManutencao);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('submits the maintenance details and changes the displayed state on success', () => {
    component.formManutencao.setValue({
      descricaoManutencao: '  Troca da fonte  ',
      orientacoesCliente: '  Usar filtro de linha  '
    });

    component.salvarManutencao();

    expect(efetuarManutencao).toHaveBeenCalledWith(42, {
      descricaoManutencao: 'Troca da fonte',
      orientacoesCliente: 'Usar filtro de linha'
    });
    expect(component.solicitacao?.estado).toBe(EstadoSolicitacao.ARRUMADA);
    expect(component.mensagem?.titulo).toBe('Manutenção Efetuada!');
    expect(component.processando).toBe(false);
  });

  it('keeps the form available and reports API errors', () => {
    efetuarManutencao.mockReturnValueOnce(throwError(() => ({
      error: { mensagem: 'A solicitação não pode ser finalizada neste estado.' }
    })));
    component.formManutencao.setValue({
      descricaoManutencao: 'Troca da fonte',
      orientacoesCliente: 'Usar filtro de linha'
    });

    component.salvarManutencao();

    expect(component.erroOperacao).toBe('A solicitação não pode ser finalizada neste estado.');
    expect(component.mensagem).toBeNull();
    expect(component.processando).toBe(false);
  });
});
