import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { perfilGuard } from './perfil.guard';
import { AuthService } from '../services/auth.service';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('perfilGuard', () => {
  let authService: AuthService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: Router,
          useValue: {
            createUrlTree: vi.fn((commands) => ({ path: commands.join('/') } as unknown as UrlTree))
          }
        }
      ]
    });

    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  it('should redirect to /login if not logged in', () => {
    vi.spyOn(authService, 'isLoggedIn').mockReturnValue(false);

    const guard = perfilGuard('CLIENTE');
    const result = TestBed.runInInjectionContext(() => guard({} as any, {} as any));

    expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
    expect(result).toEqual({ path: '/login' });
  });

  it('should allow access if user has expected role', () => {
    vi.spyOn(authService, 'isLoggedIn').mockReturnValue(true);
    vi.spyOn(authService, 'perfil').mockReturnValue('CLIENTE');

    const guard = perfilGuard('CLIENTE');
    const result = TestBed.runInInjectionContext(() => guard({} as any, {} as any));

    expect(result).toBe(true);
  });

  it('should redirect to /cliente/home if CLIENTE attempts to access FUNCIONARIO route', () => {
    vi.spyOn(authService, 'isLoggedIn').mockReturnValue(true);
    vi.spyOn(authService, 'perfil').mockReturnValue('CLIENTE');

    const guard = perfilGuard('FUNCIONARIO');
    const result = TestBed.runInInjectionContext(() => guard({} as any, {} as any));

    expect(router.createUrlTree).toHaveBeenCalledWith(['/cliente/home']);
    expect(result).toEqual({ path: '/cliente/home' });
  });

  it('should redirect to /funcionario/home if FUNCIONARIO attempts to access CLIENTE route', () => {
    vi.spyOn(authService, 'isLoggedIn').mockReturnValue(true);
    vi.spyOn(authService, 'perfil').mockReturnValue('FUNCIONARIO');

    const guard = perfilGuard('CLIENTE');
    const result = TestBed.runInInjectionContext(() => guard({} as any, {} as any));

    expect(router.createUrlTree).toHaveBeenCalledWith(['/funcionario/home']);
    expect(result).toEqual({ path: '/funcionario/home' });
  });
});

