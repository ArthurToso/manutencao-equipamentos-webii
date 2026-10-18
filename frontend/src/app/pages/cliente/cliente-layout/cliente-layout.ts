import { Component, computed, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  imports: [RouterOutlet],
  selector: 'app-cliente-layout',
  styleUrl: './cliente-layout.css',
  templateUrl: './cliente-layout.html',
})
export class ClienteLayout {
  private authService = inject(AuthService);
  private router = inject(Router);

  readonly nome = computed(() => this.authService.usuarioAtual()?.nome ?? '');

  sair(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
