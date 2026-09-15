import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FuncionarioOrcamento } from './funcionario-orcamento';

describe('FuncionarioOrcamento', () => {
  let component: FuncionarioOrcamento;
  let fixture: ComponentFixture<FuncionarioOrcamento>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FuncionarioOrcamento],
    }).compileComponents();

    fixture = TestBed.createComponent(FuncionarioOrcamento);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
