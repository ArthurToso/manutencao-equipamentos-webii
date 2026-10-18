import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const perfilGuard = (perfilEsperado: 'CLIENTE' | 'FUNCIONARIO'): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isLoggedIn()) {
      return router.createUrlTree(['/login']);
    }

    if (authService.perfil() === perfilEsperado) {
      return true;
    }

    const perfilAtual = authService.perfil();
    if (perfilAtual === 'CLIENTE') {
      return router.createUrlTree(['/cliente/home']);
    } else if (perfilAtual === 'FUNCIONARIO') {
      return router.createUrlTree(['/funcionario/home']);
    }

    return router.createUrlTree(['/login']);
  };
};

