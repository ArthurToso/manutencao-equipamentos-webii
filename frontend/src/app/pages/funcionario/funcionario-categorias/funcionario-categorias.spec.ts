import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FuncionarioCategorias } from './funcionario-categorias';

describe('FuncionarioCategorias', () => {
  let component: FuncionarioCategorias;
  let fixture: ComponentFixture<FuncionarioCategorias>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FuncionarioCategorias],
    }).compileComponents();

    fixture = TestBed.createComponent(FuncionarioCategorias);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
