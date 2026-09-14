import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClienteSolicitacao } from './cliente-solicitacao';

describe('ClienteSolicitacao', () => {
  let component: ClienteSolicitacao;
  let fixture: ComponentFixture<ClienteSolicitacao>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClienteSolicitacao],
    }).compileComponents();

    fixture = TestBed.createComponent(ClienteSolicitacao);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
