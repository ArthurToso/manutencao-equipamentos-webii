import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
import { SolicitacaoService } from './solicitacao.service';

describe('SolicitacaoService', () => {
  let service: SolicitacaoService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(SolicitacaoService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('sends a PUT request to finalize the solicitation', () => {
    service.finalizarServico(42).subscribe();

    const request = httpTestingController.expectOne(
      `${environment.apiUrl}/solicitacoes/42/finalizar`
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({});
    request.flush({ id: 42, estado: 'FINALIZADA' });
  });

  it('sends maintenance details to the API', () => {
    const dados = {
      descricaoManutencao: 'Substituição da fonte',
      orientacoesCliente: 'Utilizar filtro de linha'
    };

    service.efetuarManutencao(42, dados).subscribe();

    const request = httpTestingController.expectOne(
      `${environment.apiUrl}/solicitacoes/42/manutencao`
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(dados);
    request.flush({ id: 42, estado: 'ARRUMADA' });
  });

  it('sends the selected employee when redirecting a solicitation', () => {
    const dados = { funcionarioId: 7 };

    service.redirecionar(42, dados).subscribe();

    const request = httpTestingController.expectOne(
      `${environment.apiUrl}/solicitacoes/42/redirecionar`
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(dados);
    request.flush({ id: 42, estado: 'REDIRECIONADA' });
  });
});
