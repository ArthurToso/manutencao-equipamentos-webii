import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClienteNovaSolicitacao } from './cliente-nova-solicitacao';

describe('ClienteNovaSolicitacao', () => {
  let component: ClienteNovaSolicitacao;
  let fixture: ComponentFixture<ClienteNovaSolicitacao>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClienteNovaSolicitacao],
    }).compileComponents();

    fixture = TestBed.createComponent(ClienteNovaSolicitacao);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
