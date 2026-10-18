import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  let clonedReq = req;

  if (req.url.startsWith(environment.apiUrl)) {
    const token = authService.getToken();
    if (token) {
      clonedReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
  }

  return next(clonedReq).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse) {
        if (err.status === 401) {
          const isLoginRequest = req.url.includes('/auth/login');
          if (!isLoginRequest) {
            authService.logout();
            router.navigate(['/login']);
          }
        } else if (err.status === 403) {
          const perfil = authService.perfil();
          if (perfil === 'CLIENTE') {
            router.navigate(['/cliente/home']);
          } else if (perfil === 'FUNCIONARIO') {
            router.navigate(['/funcionario/home']);
          }
        }
      }
      return throwError(() => err);
    })
  );
};

