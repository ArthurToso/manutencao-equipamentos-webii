import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AuthService);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should start with no logged in user', () => {
    expect(service.isLoggedIn()).toBe(false);
    expect(service.usuarioAtual()).toBeNull();
    expect(service.perfil()).toBeNull();
    expect(service.getToken()).toBeNull();
  });

  it('should clear signals and localStorage on logout', () => {
    service.usuarioAtual.set({ id: 1, nome: 'Teste', email: 'teste@example.com', perfil: 'CLIENTE' });
    expect(service.isLoggedIn()).toBe(true);
    service.logout();
    expect(service.isLoggedIn()).toBe(false);
    expect(service.usuarioAtual()).toBeNull();
    expect(service.getToken()).toBeNull();
  });
});

