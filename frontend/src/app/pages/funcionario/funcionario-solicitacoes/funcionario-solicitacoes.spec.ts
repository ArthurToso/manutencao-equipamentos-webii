import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FuncionarioSolicitacoes } from './funcionario-solicitacoes';

describe('FuncionarioSolicitacoes', () => {
  let component: FuncionarioSolicitacoes;
  let fixture: ComponentFixture<FuncionarioSolicitacoes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FuncionarioSolicitacoes],
    }).compileComponents();

    fixture = TestBed.createComponent(FuncionarioSolicitacoes);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
