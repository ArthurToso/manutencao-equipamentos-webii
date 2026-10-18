import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginDTO, LoginResponseDTO, UsuarioDTO } from '../shared';

export interface AuthSession {
  token: string;
  tipo: string;
  expiraEm: number | string;
  expiresAt: number;
  usuario: UsuarioDTO;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly storageKey = 'auth';
  private http = inject(HttpClient);

  readonly usuarioAtual = signal<UsuarioDTO | null>(null);
  readonly isLoggedIn = computed(() => !!this.usuarioAtual());
  readonly perfil = computed(() => this.usuarioAtual()?.perfil ?? null);

  constructor() {
    this.restaurarSessao();
  }

  login(emailOrDto: string | LoginDTO, maybeSenha?: string): Observable<LoginResponseDTO> {
    const body: LoginDTO = typeof emailOrDto === 'string'
      ? { email: emailOrDto, senha: maybeSenha ?? '' }
      : emailOrDto;

    return this.http.post<LoginResponseDTO>(`${environment.apiUrl}/auth/login`, body).pipe(
      tap((res) => {
        let expiresAt: number;
        if (typeof res.expiraEm === 'number') {
          expiresAt = res.expiraEm < 10_000_000_000
            ? Date.now() + res.expiraEm * 1000
            : res.expiraEm;
        } else {
          expiresAt = new Date(res.expiraEm).getTime();
        }

        const session: AuthSession = {
          token: res.token,
          tipo: res.tipo || 'Bearer',
          expiraEm: res.expiraEm,
          expiresAt,
          usuario: res.usuario
        };

        try {
          localStorage.setItem(this.storageKey, JSON.stringify(session));
        } catch (e) {
          console.error('Erro ao salvar autenticação no localStorage', e);
        }

        this.usuarioAtual.set(res.usuario);
      })
    );
  }

  logout(): void {
    try {
      localStorage.removeItem(this.storageKey);
    } catch (e) {
      console.error('Erro ao remover autenticação do localStorage', e);
    }
    this.usuarioAtual.set(null);
  }

  getToken(): string | null {
    try {
      const raw = localStorage.getItem(this.storageKey);
      if (!raw) {
        return null;
      }
      const session = JSON.parse(raw) as AuthSession;
      if (!session || !session.token) {
        return null;
      }

      if (session.expiresAt && Date.now() >= session.expiresAt) {
        this.logout();
        return null;
      }

      return session.token;
    } catch {
      return null;
    }
  }

  private restaurarSessao(): void {
    try {
      const raw = localStorage.getItem(this.storageKey);
      if (!raw) {
        return;
      }

      const session = JSON.parse(raw) as AuthSession;
      if (!session || !session.token || !session.usuario) {
        this.logout();
        return;
      }

      let expiresAt = session.expiresAt;
      if (!expiresAt) {
        if (typeof session.expiraEm === 'number') {
          expiresAt = session.expiraEm < 10_000_000_000
            ? Date.now() + session.expiraEm * 1000
            : session.expiraEm;
        } else if (session.expiraEm) {
          expiresAt = new Date(session.expiraEm).getTime();
        }
      }

      if (expiresAt && Date.now() >= expiresAt) {
        this.logout();
        return;
      }

      this.usuarioAtual.set(session.usuario);
    } catch {
      this.logout();
    }
  }
}

