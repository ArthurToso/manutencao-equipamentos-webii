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

    service.efetuarManutencao(42, dados).subscribe(resultado => {
      expect(resultado.estado).toBe('ARRUMADA');
      expect(resultado.descricaoManutencao).toBe(dados.descricaoManutencao);
      expect(resultado.orientacoesCliente).toBe(dados.orientacoesCliente);
    });

    const request = httpTestingController.expectOne(
      `${environment.apiUrl}/solicitacoes/42/manutencao`
    );
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(dados);
    request.flush({
      id: 42,
      estado: 'ARRUMADA',
      descricaoManutencao: dados.descricaoManutencao,
      orientacoesCliente: dados.orientacoesCliente
    });
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

  it('sends GET request to list solicitations for funcionario with params and maps them', () => {
    service.listarTodas('PERIODO', '2026-09-01', '2026-09-30').subscribe(resultado => {
      expect(resultado.length).toBe(1);
      expect(resultado[0].id).toBe(10);
      expect(resultado[0].descricaoEquipamento).toBe('Mouse Razer');
      expect(resultado[0].estado).toBe('REDIRECIONADA');
      expect(resultado[0].nomeCliente).toBe('José');
      expect(resultado[0].dataHora instanceof Date).toBe(true);
    });

    const request = httpTestingController.expectOne(
      `${environment.apiUrl}/solicitacoes?filtro=PERIODO&dataInicial=2026-09-01&dataFinal=2026-09-30`
    );
    expect(request.request.method).toBe('GET');
    request.flush([
      {
        id: 10,
        descricaoEquipamento: 'Mouse Razer',
        dataHora: '2026-09-15T10:00:00',
        estado: 'REDIRECIONADA',
        nomeCliente: 'José'
      }
    ]);
  });
});
