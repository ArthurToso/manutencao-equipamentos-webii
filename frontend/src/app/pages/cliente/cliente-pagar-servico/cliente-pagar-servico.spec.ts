import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ClientePagarServico } from './cliente-pagar-servico';

describe('ClientePagarServico', () => {
  let component: ClientePagarServico;
  let fixture: ComponentFixture<ClientePagarServico>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientePagarServico],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClientePagarServico);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
