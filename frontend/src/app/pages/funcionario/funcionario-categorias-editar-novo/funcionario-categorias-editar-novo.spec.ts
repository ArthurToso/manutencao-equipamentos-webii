import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FuncionarioCategoriasEditarNovo } from './funcionario-categorias-editar-novo';

describe('FuncionarioCategoriasEditarNovo', () => {
  let component: FuncionarioCategoriasEditarNovo;
  let fixture: ComponentFixture<FuncionarioCategoriasEditarNovo>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FuncionarioCategoriasEditarNovo],
    }).compileComponents();

    fixture = TestBed.createComponent(FuncionarioCategoriasEditarNovo);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
