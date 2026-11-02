import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, Router, provideRouter } from '@angular/router';
import { By } from '@angular/platform-browser';
import { describe, it, expect, afterEach, vi } from 'vitest';
import { ClienteOrcamento } from './cliente-orcamento';
import { environment } from '../../../../environments/environment';
import { SolicitacaoApiDTO } from '../../../shared';

describe('ClienteOrcamento', () => {
  let component: ClienteOrcamento;
  let fixture: ComponentFixture<ClienteOrcamento>;
  let httpMock: HttpTestingController;
  let router: Router;

  const mockOrcada: SolicitacaoApiDTO = {
    id: 14,
    descricaoEquipamento: 'Notebook/Teclado Microsoft Wired 600',
    categoriaId: 5,
    categoriaNome: 'Teclado',
    descricaoDefeito: 'Líquido derramado, teclas grudando.',
    dataHora: '2026-09-29T16:20:00',
    estado: 'ORCADA',
    clienteId: 2,
    valorOrcamento: 70.00
  };

  function setup(paramId: string = '14') {
    TestBed.configureTestingModule({
      imports: [ClienteOrcamento],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => (key === 'id' ? paramId : null)
              }
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClienteOrcamento);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate');
  }

  afterEach(() => {
    httpMock.verify();
  });

  it('carrega e mostra o valor em destaque formatado', () => {
    setup('14');
    fixture.detectChanges();

    const req = httpMock.expectOne(`${environment.apiUrl}/solicitacoes/14/orcamento`);
    expect(req.request.method).toBe('GET');
    req.flush(mockOrcada);

    fixture.detectChanges();

    expect(component.estadoTela).toBe('pronto');
    const valorEl = fixture.debugElement.query(By.css('.valor-destaque__valor'));
    expect(valorEl).toBeTruthy();
    expect(valorEl.nativeElement.textContent.trim()).toBe('R$ 70,00');
  });

  it('estado diferente de ORCADA esconde os botões e exibe aviso', () => {
    setup('14');
    fixture.detectChanges();

    const req = httpMock.expectOne(`${environment.apiUrl}/solicitacoes/14/orcamento`);
    req.flush({ ...mockOrcada, estado: 'APROVADA' });

    fixture.detectChanges();

    expect(component.solicitacao?.estado).toBe('APROVADA');
    const avisoEl = fixture.debugElement.query(By.css('.aviso-orcamento'));
    expect(avisoEl).toBeTruthy();
    expect(avisoEl.nativeElement.textContent).toContain('Este orçamento não está aguardando a sua decisão');

    const acoesEl = fixture.debugElement.query(By.css('.form-actions button'));
    expect(acoesEl).toBeNull();
  });

  it('aprovar com sucesso mostra "Serviço Aprovado no Valor R$ ..." e só navega para /cliente/home ao clicar em OK', fakeAsync(() => {
    setup('14');
    fixture.detectChanges();

    const getReq = httpMock.expectOne(`${environment.apiUrl}/solicitacoes/14/orcamento`);
    getReq.flush(mockOrcada);
    fixture.detectChanges();

    const aprovarBtn = fixture.debugElement.query(By.css('.btn--primary'));
    expect(aprovarBtn).toBeTruthy();
    aprovarBtn.nativeElement.click();

    fixture.detectChanges();
    expect(component.processando).toBe(true);

    const putReq = httpMock.expectOne(`${environment.apiUrl}/solicitacoes/14/aprovar`);
    expect(putReq.request.method).toBe('PUT');
    expect(putReq.request.body).toBeNull();
    putReq.flush({ ...mockOrcada, estado: 'APROVADA', valorOrcamento: 70.00 });

    tick();
    fixture.detectChanges();

    expect(component.processando).toBe(false);
    expect(component.mensagem).toBeTruthy();
    expect(component.mensagem?.texto).toBe('Serviço Aprovado no Valor R$ 70,00');

    const modalTexto = fixture.debugElement.query(By.css('.modal-box__texto'));
    expect(modalTexto.nativeElement.textContent).toBe('Serviço Aprovado no Valor R$ 70,00');
    expect(router.navigate).not.toHaveBeenCalled();

    const btnOk = fixture.debugElement.query(By.css('.modal-box button'));
    expect(btnOk).toBeTruthy();
    btnOk.nativeElement.click();

    expect(router.navigate).toHaveBeenCalledWith(['/cliente/home']);
  }));

  it('erro 409 mostra a mensagem e mantém a tela', () => {
    setup('14');
    fixture.detectChanges();

    const req = httpMock.expectOne(`${environment.apiUrl}/solicitacoes/14/orcamento`);
    req.flush(
      { status: 409, erro: 'Conflito de Estado', mensagem: 'A solicitação ainda não foi orçada' },
      { status: 409, statusText: 'Conflict' }
    );

    fixture.detectChanges();

    expect(component.estadoTela).toBe('erro');
    expect(component.mensagemErro).toBe('A solicitação ainda não foi orçada');
    const erroEl = fixture.debugElement.query(By.css('.alert-danger'));
    expect(erroEl).toBeTruthy();
    expect(erroEl.nativeElement.textContent).toContain('A solicitação ainda não foi orçada');
  });

  it('404 mostra "Solicitação não encontrada"', () => {
    setup('9999');
    fixture.detectChanges();

    const req = httpMock.expectOne(`${environment.apiUrl}/solicitacoes/9999/orcamento`);
    req.flush(
      { status: 404, erro: 'Não Encontrado', mensagem: 'Solicitação não encontrada' },
      { status: 404, statusText: 'Not Found' }
    );

    fixture.detectChanges();

    expect(component.estadoTela).toBe('erro');
    expect(component.mensagemErro).toBe('Solicitação não encontrada');
    const erroEl = fixture.debugElement.query(By.css('.alert-danger'));
    expect(erroEl).toBeTruthy();
    expect(erroEl.nativeElement.textContent).toContain('Solicitação não encontrada');
  });

  it('rota com id inválido (/abc) exibe erro amigável sem chamar a API', () => {
    setup('abc');
    fixture.detectChanges();

    httpMock.expectNone(`${environment.apiUrl}/solicitacoes/abc/orcamento`);
    expect(component.estadoTela).toBe('erro');
    expect(component.mensagemErro).toBe('Solicitação não encontrada');
    const erroEl = fixture.debugElement.query(By.css('.alert-danger'));
    expect(erroEl).toBeTruthy();
  });
});