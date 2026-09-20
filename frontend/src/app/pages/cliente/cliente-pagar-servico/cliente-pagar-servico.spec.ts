import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClientePagarServico } from './cliente-pagar-servico';

describe('ClientePagarServico', () => {
  let component: ClientePagarServico;
  let fixture: ComponentFixture<ClientePagarServico>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientePagarServico],
    }).compileComponents();

    fixture = TestBed.createComponent(ClientePagarServico);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
