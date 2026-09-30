import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FuncionarioFuncionarios } from './funcionario-funcionarios';

describe('FuncionarioFuncionarios', () => {
  let component: FuncionarioFuncionarios;
  let fixture: ComponentFixture<FuncionarioFuncionarios>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FuncionarioFuncionarios],
    }).compileComponents();

    fixture = TestBed.createComponent(FuncionarioFuncionarios);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
