import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';
import { environment } from '../../environments/environment';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authService: AuthService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        {
          provide: Router,
          useValue: { navigate: vi.fn() }
        }
      ]
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should attach Authorization header when URL is under apiUrl and token exists', () => {
    vi.spyOn(authService, 'getToken').mockReturnValue('jwt-token-123');

    http.get(`${environment.apiUrl}/categorias`).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/categorias`);
    expect(req.request.headers.has('Authorization')).toBe(true);
    expect(req.request.headers.get('Authorization')).toBe('Bearer jwt-token-123');
    req.flush([]);
  });

  it('should NOT attach Authorization header for external URLs like viacep', () => {
    vi.spyOn(authService, 'getToken').mockReturnValue('jwt-token-123');

    http.get('https://viacep.com.br/ws/80000000/json/').subscribe();

    const req = httpMock.expectOne('https://viacep.com.br/ws/80000000/json/');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('should logout and navigate to /login on 401 from protected endpoint', () => {
    const logoutSpy = vi.spyOn(authService, 'logout');
    const navigateSpy = vi.spyOn(router, 'navigate');

    http.get(`${environment.apiUrl}/solicitacoes/minhas`).subscribe({
      error: () => {}
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/solicitacoes/minhas`);
    req.flush({ mensagem: 'Não autorizado' }, { status: 401, statusText: 'Unauthorized' });

    expect(logoutSpy).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should NOT logout nor navigate on 401 from /auth/login endpoint', () => {
    const logoutSpy = vi.spyOn(authService, 'logout');
    const navigateSpy = vi.spyOn(router, 'navigate');

    http.post(`${environment.apiUrl}/auth/login`, {}).subscribe({
      error: () => {}
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    req.flush({ mensagem: 'E-mail ou senha inválidos' }, { status: 401, statusText: 'Unauthorized' });

    expect(logoutSpy).not.toHaveBeenCalled();
    expect(navigateSpy).not.toHaveBeenCalled();
  });

  it('should redirect to profile home on 403 without logging out', () => {
    const logoutSpy = vi.spyOn(authService, 'logout');
    const navigateSpy = vi.spyOn(router, 'navigate');
    vi.spyOn(authService, 'perfil').mockReturnValue('CLIENTE');

    http.post(`${environment.apiUrl}/categorias`, {}).subscribe({
      error: () => {}
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/categorias`);
    req.flush({ mensagem: 'Acesso proibido' }, { status: 403, statusText: 'Forbidden' });

    expect(logoutSpy).not.toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/cliente/home']);
  });
});

