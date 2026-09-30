import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FuncionarioFuncionariosEditarNovo } from './funcionario-funcionarios-editar-novo';

describe('FuncionarioFuncionariosEditarNovo', () => {
  let component: FuncionarioFuncionariosEditarNovo;
  let fixture: ComponentFixture<FuncionarioFuncionariosEditarNovo>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FuncionarioFuncionariosEditarNovo],
    }).compileComponents();

    fixture = TestBed.createComponent(FuncionarioFuncionariosEditarNovo);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
